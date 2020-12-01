package com.HIT.weisongzhao;

import java.util.ArrayList;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import bilib.tools.NumFormat;
import deconvolutionSACD.Stats;
import deconvolutionSACD.algorithm.Constraint;
import deconvolutionSACD.algorithm.Controller;
import deconvolutionSACDlab.Lab;
import deconvolutionSACDlab.Platform;
import deconvolutionSACDlab.monitor.Monitors;
import deconvolutionSACDlab.monitor.Verbose;
import deconvolutionSACDlab.output.Output;
import fftSACD.AbstractFFT;
import fftSACD.FFT;
import signalSACD.RealSignal;
import signalSACD.SignalCollector;
import signalSACD.apodization.Apodization;
import signalSACD.padding.Padding;
/**
 * This class is the common part of every Algorithm_new of deconvolution.
 * 
 * @author Daniel Sage
 * 
 */
public abstract class Algorithm_new implements Callable<RealSignal> {

	/** y is the input signal of the deconvolution. */
	protected RealSignal			y;

	/** h is the PSF signal for the deconvolution. */
	protected RealSignal			h;

	protected boolean				threaded;

	/** Optimized implementation in term of memory footprint */
	protected boolean				optimizedMemoryFootprint;

	protected int N = 1;
	
	protected AbstractFFT fft;
	protected Controller controller;
	
	public Algorithm_new() {
		setController(new Controller());
		optimizedMemoryFootprint = true;
		threaded = true;
		fft = FFT.getFastestFFT().getDefaultFFT();
	}
	
	public Algorithm_new(Controller controller) {
		this.controller = controller;
		optimizedMemoryFootprint = true;
		threaded = true;
		fft = FFT.getFastestFFT().getDefaultFFT();
	}

	public void setOptimizedMemoryFootprint(boolean optimizedMemoryFootprint) {
		this.optimizedMemoryFootprint = optimizedMemoryFootprint;
	}

	public abstract String getName();
	public abstract String[] getShortnames();
	public abstract double getMemoryFootprintRatio();
	public abstract int getComplexityNumberofFFT();
	public abstract boolean isRegularized();
	public abstract boolean isStepControllable();
	public abstract boolean isIterative();
	public abstract boolean isWaveletsBased();
	public abstract Algorithm_new setParameters(double... params);
	public abstract double getRegularizationFactor();
	public abstract double getStepFactor();
	public abstract double[] getParameters();

	public abstract double[] getDefaultParameters();

	public RealSignal run(RealSignal image, RealSignal psf, RealSignal ref) {
		if (ref!=null)
			setReference(ref);
		return run(image, psf);
	}

	public RealSignal run(RealSignal image, RealSignal psf) {

		String sn = getShortnames()[0];
		String algoParam = sn + "(" + getParametersAsString() + ")";
		//if (controller.isSystem())
		//	SystemInfo.activate();

		Padding pad = controller.getPadding();
		Apodization apo = controller.getApodization();
		double norm = controller.getNormalizationPSF();
		
		controller.setAlgoName(algoParam);
		fft = controller.getFFT();
		controller.setIterationsMax(N);

		if (image == null)
			return null;
		
		if (psf == null)
			return null;
		// Prepare the controller and the outputs
		Monitors monitors = controller.getMonitors();
		monitors.setVerbose(controller.getVerbose());
		monitors.log("Path: " + controller.toStringPath());
		monitors.log("Algorithm_new: " + getName());
		
		// Prepare the signal and the PSF
		y = pad.pad(monitors, image);
		y.setName("y");
		apo.apodize(monitors, y);
		monitors.log("Input: " + y.dimAsString());
		h = psf.changeSizeAs(y);
		h.setName("h");
		h.normalize(norm);
		monitors.log("PSF: " + h.dimAsString() + " normalized " + (norm <= 0 ? "no" : norm));

		String iterations = (isIterative() ? N + " iterations" : "direct");

		controller.setIterationsMax(N);
		
		monitors.log(sn + " is starting (" + iterations + ")");
		controller.setMonitors(monitors);

		controller.start(y);
		h.circular();

		// FFT
		fft.init( monitors,y.nx, y.ny, y.nz);
		controller.setFFT(fft);
		
		monitors.log(sn + " data ready");
		monitors.log(algoParam);

		RealSignal x = null;

		try {
			if (threaded == true) {
				ExecutorService pool = Executors.newSingleThreadExecutor();
				Future<RealSignal> future = pool.submit(this);
				x = future.get();
			}
			else {
				x = call();
			}
		}
		catch (InterruptedException ex) {
			ex.printStackTrace();
			x = y.duplicate();
		}
		catch (ExecutionException ex) {
			ex.printStackTrace();
			x = y.duplicate();
		}
		catch (Exception e) {
			e.printStackTrace();
			x = y.duplicate();
		}
		SignalCollector.free(y);
		SignalCollector.free(h);
		x.setName("x");
		RealSignal result = pad.crop(monitors, x);
		
		controller.finish(result);
		monitors.log(getName() + " is finished");

		SignalCollector.free(x);
		
		if (controller.getOuts().size() == 0)
		if (Lab.getPlatform() == Platform.IMAGEJ || Lab.getPlatform() == Platform.ICY)
			Lab.show(monitors, result, "Result of " + sn);

		result.setName("Out of " + algoParam);
		
		monitors.log("End of " + sn + " in " + NumFormat.seconds(controller.getTimeNano()) + " and " + controller.getMemoryAsString());

		return result;
	}
	public RealSignal run_new(RealSignal image ) {

		String sn = getShortnames()[0];
		String algoParam = sn + "(" + getParametersAsString() + ")";
		//if (controller.isSystem())
		//	SystemInfo.activate();

		Padding pad = controller.getPadding();
		Apodization apo = controller.getApodization();
		
		controller.setAlgoName(algoParam);
		fft = controller.getFFT();
		controller.setIterationsMax(1);

		if (image == null)
			return null;
		
		// Prepare the controller and the outputs
		Monitors monitors = controller.getMonitors();
		monitors.setVerbose(controller.getVerbose());
		monitors.log("Path: " + controller.toStringPath());
		monitors.log("Algorithm_new: " + getName());
		
		// Prepare the signal and the PSF
		y = pad.pad(monitors, image);
		y.setName("y");
		apo.apodize(monitors, y);
		monitors.log("Input: " + y.dimAsString());

		String iterations = (isIterative() ? N + " iterations" : "direct");

		controller.setIterationsMax(1);
		
		monitors.log(sn + " is starting (" + iterations + ")");
		controller.setMonitors(monitors);

		controller.start(y);

		// FFT
		fft.init(monitors, y.nx, y.ny, y.nz);
		controller.setFFT(fft);
		
		monitors.log(sn + " data ready");
		monitors.log(algoParam);

		RealSignal x = null;

		try {
			if (threaded == true) {
				ExecutorService pool = Executors.newSingleThreadExecutor();
				Future<RealSignal> future = pool.submit(this);
				x = future.get();
			}
			else {
				x = call();
			}
		}
		catch (InterruptedException ex) {
			ex.printStackTrace();
			x = y.duplicate();
		}
		catch (ExecutionException ex) {
			ex.printStackTrace();
			x = y.duplicate();
		}
		catch (Exception e) {
			e.printStackTrace();
			x = y.duplicate();
		}
		SignalCollector.free(y);
		x.setName("x");
		RealSignal result = pad.crop(monitors, x);
		
		controller.finish(result);
		monitors.log(getName() + " is finished");

		SignalCollector.free(x);
		
		if (controller.getOuts().size() == 0)
		if (Lab.getPlatform() == Platform.IMAGEJ || Lab.getPlatform() == Platform.ICY)
			Lab.show(monitors, result, "Result of " + sn);

		result.setName("Out of " + algoParam);
		
		monitors.log("End of " + sn + " in " + NumFormat.seconds(controller.getTimeNano()) + " and " + controller.getMemoryAsString());

		return result;
	}
	public Algorithm_new setController(Controller controller) {
		this.controller = controller;
		return this;
	}

	public Controller getController() {
		return controller;
	}

	public int getIterationsMax() {
		return N;
	}

	public int getIterations() {
		return controller.getIterations();
	}

	public double getTime() {
		return controller.getTimeNano();
	}

	public double getMemory() {
		return controller.getMemory();
	}
	
	public double getResidu() {
		return controller.getResidu();	
	}
	
	public double getSNR() {
		return controller.getSNR();	
	}

	public double getPSNR() {
		return controller.getPSNR();	
	}

	public void setWavelets(String waveletsName) {
	}

	@Override
	public String toString() {
		String s = "";
		s += getName();
		s += (isIterative() ? ", " + N + " iterations" : " (direct)");
		s += (isRegularized() ? ", &lambda;=" + NumFormat.nice(getRegularizationFactor()) : "");
		s += (isStepControllable() ? ", &gamma;=" + NumFormat.nice(getStepFactor()) : "");
		return s;
	}

	public String getParametersAsString() {
		double p[] = getParameters();
		String param = "";
		for (int i = 0; i < p.length; i++)
			if (i == p.length - 1)
				param += NumFormat.nice(p[i]);
			else
				param += NumFormat.nice(p[i]) + ", ";
		return param;
	}

	public AbstractFFT getFFT() {
		return controller.getFFT();
	}

	public Algorithm_new setFFT(AbstractFFT fft) {
		this.fft = fft;
		controller.setFFT(fft);
		return this;
	}

	public String getPath() {
		return controller.getPath();
	}

	public Algorithm_new setPath(String path) {
		controller.setPath(path);
		return this;
	}

	public boolean isSystem() {
		return controller.isSystem();
	}

	public Algorithm_new enableSystem() {
		controller.setSystem(true);
		return this;
	}

	public Algorithm_new disableSystem() {
		controller.setSystem(false);
		return this;
	}

	public boolean isMultithreading() {
		return controller.isMultithreading();
	}

	public Algorithm_new enableMultithreading() {
		controller.setMultithreading(true);
		return this;
	}

	public Algorithm_new disableMultithreading() {
		controller.setMultithreading(false);
		return this;
	}

	public double getNormalizationPSF() {
		return controller.getNormalizationPSF();
	}

	public Algorithm_new setNormalizationPSF(double normalizationPSF) {
		controller.setNormalizationPSF(normalizationPSF);
		return this;
	}

	public double getEpsilon() {
		return controller.getEpsilon();
	}

	public Algorithm_new setEpsilon(double epsilon) {
		controller.setEpsilon(epsilon);
		return this;
	}

	public Padding getPadding() {
		return controller.getPadding();
	}

	public Algorithm_new setPadding(Padding padding) {
		controller.setPadding(padding);
		return this;
	}

	public Apodization getApodization() {
		return controller.getApodization();
	}

	public Algorithm_new setApodization(Apodization apodization) {
		controller.setApodization(apodization);
		return this;
	}

	public Monitors getMonitors() {
		return controller.getMonitors();
	}

	public Algorithm_new setMonitors(Monitors monitors) {
		controller.setMonitors(monitors);
		return this;
	}

	public Verbose getVerbose() {
		return controller.getVerbose();
	}

	public Algorithm_new setVerbose(Verbose verbose) {
		controller.setVerbose(verbose);
		return this;
	}

	public Constraint getConstraint() {
		return controller.getConstraint();
	}

	public Algorithm_new setConstraint(Constraint constraint) {
		controller.setConstraint(constraint);
		return this;
	}

	public Stats getStats() {
		return controller.getStats();
	}

	public Algorithm_new setStats(Stats stats) {
		controller.setStats(stats);
		return this;
	}
	
	public Algorithm_new showStats() {
		controller.setStats(new Stats(Stats.Mode.SHOW, "stats"));
		return this;
	}
	
	public Algorithm_new saveStats(Stats stats) {
		controller.setStats(new Stats(Stats.Mode.SAVE, "stats"));
		return this;
	}
	
	public Algorithm_new setStats() {
		controller.setStats(new Stats(Stats.Mode.SHOWSAVE, "stats"));
		return this;
	}

	public Algorithm_new showStats(String name) {
		controller.setStats(new Stats(Stats.Mode.SHOW, name));
		return this;
	}
	
	public Algorithm_new saveStats(Stats stats, String name) {
		controller.setStats(new Stats(Stats.Mode.SAVE, name));
		return this;
	}
	
	public Algorithm_new setStats(String name) {
		controller.setStats(new Stats(Stats.Mode.SHOWSAVE, name));
		return this;
	}

	public double getResiduMin() {
		return controller.getResiduMin();
	}

	public Algorithm_new setResiduMin(double residuMin) {
		controller.setResiduMin(residuMin);
		return this;
	}

	public double getTimeLimit() {
		return controller.getTimeLimit();
	}

	public Algorithm_new setTimeLimit(double timeLimit) {
		controller.setTimeLimit(timeLimit);
		return this;
	}

	public RealSignal getReference() {
		return controller.getReference();
	}

	public Algorithm_new setReference(RealSignal ref) {
		controller.setReference(ref);
		return this;
	}

	public ArrayList<Output> getOuts() {
		return controller.getOuts();
	}

	public Algorithm_new setOuts(ArrayList<Output> outs) {
		controller.setOuts(outs);
		return this;
	}

	public Algorithm_new addOutput(Output out) {
		controller.addOutput(out);
		return this;
	}
	
	public String getParametersToString() {
		double params[] = getParameters();
		if (params != null) {
			if (params.length > 0) {
				String s = " ";
				for (double param : params)
					s += NumFormat.nice(param) + " ";
				return s;
			}
		}
		return "parameter-free";
	}
}
