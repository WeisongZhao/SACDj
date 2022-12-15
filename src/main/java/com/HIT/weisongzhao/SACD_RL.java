// Conditions of use: You are free to use this software for research or
// educational purposes. In addition, we expect you to include adequate
// citations and acknowledgments whenever you present or publish results that
// are based on it.
//% *********************************************************************************
//% It is a part of publication:
//% Weisong Zhao et al. Enhancing detectable fluorescence fluctuation for  
//% high-throughput and four-dimensional live-cell super-resolution imaging
//% , Nature Photonics (2022).
//% *********************************************************************************
//%    Copyright 2019~2022 Weisong Zhao et al.
//%
//%    This program is free software: you can redistribute it and/or modify
//%    it under the terms of the Open Data Commons Open Database License v1.0.
//%
//%    This program is distributed in the hope that it will be useful,
//%    but WITHOUT ANY WARRANTY; without even the implied warranty of
//%    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//%    Open Data Commons Open Database License for more details.
//%
//%    You should have received a copy of the
//%    Open Data Commons Open Database License
//%    along with this program.  If not, see:
//%    <https://opendatacommons.org/licenses/odbl/>.

package com.HIT.weisongzhao;

import javax.swing.JDialog;

import deconvolutionSACD.algorithm.RichardsonLucyTV;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import signalSACD.RealSignal;
import signalSACD.SignalCollector;

public class SACD_RL extends JDialog implements PlugIn {
	private static int iterations1 = 10;
	private static double NA = 1.4;
	private static double lambda = 561;
	private static double lateralres = 65;
	private static double tv = 0;
	protected ImagePlus impReconstruction;

	@Override
	public void run(String arg) {

		if (IJ.versionLessThan("1.46j"))
			return;
		int[] wList = WindowManager.getIDList();
		if (wList == null) {
			IJ.noImage();
			return;
		}
		String[] titles = new String[wList.length];
		for (int i = 0; i < wList.length; i++) {
			ImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp != null)
				titles[i] = imp.getTitle();
			else
				titles[i] = "";
		}

		String titleImage = Prefs.get("SACD.titleImage", titles[0]);
		int imageChoice = 0;
		for (int i = 0; i < wList.length; i++) {
			if (titleImage.equals(titles[i])) {
				imageChoice = i;
				break;
			}
		}

		GenericDialog gd = new GenericDialog("SACD: simplified version");

		gd.addChoice("Image sequence", titles, titles[imageChoice]);
		gd.addNumericField("NA", NA, 1);
		gd.addNumericField("Wave length (nm)", lambda, 0);
		gd.addNumericField("Pixel size (nm)", lateralres, 2);
		gd.addNumericField("TV weight (value x 1e-5)", tv, 2);
		gd.addNumericField("Iterations", iterations1, 0, 5, "times");
		gd.addHelp("https://github.com/WeisongZhao/SACDj");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		// Get parameters
		NA = gd.getNextNumber();
		lambda = gd.getNextNumber();
		lateralres = gd.getNextNumber();
		tv = gd.getNextNumber();
		iterations1 = (int) gd.getNextNumber();
		ImagePlus impY = WindowManager.getImage(wList[gd.getNextChoiceIndex()]);
		if (!showDialog())
			return;
		SACD_recon(impY, NA, lambda, lateralres, iterations1, tv);
	}

	private boolean showDialog() {

		return true;
	}

	public void SACD_recon(ImagePlus imp, double NA, double lambda, double resLateral, int iterations1, double tv) {

		ImagePlus psf = SACD_BornWolf.CreatPSF(NA, lambda, resLateral);
		psf.show();
		int w = imp.getWidth(), h = imp.getHeight(), t = imp.getStackSize();		
		for (int f = 0; f < t; f = f + 1) {

			ImageStack imstep1stack = new ImageStack(w, h);
			IJ.showStatus("RL Deconvolution");
			IJ.showProgress(f, t);
			ImageStack imstack = imp.getStack();
			ImageStack inputstack = new ImageStack(w, h);
			inputstack.addSlice("", imstack.getProcessor(f + 1));
			ImagePlus input = new ImagePlus("", inputstack);
			ImagePlus imstep1 = RLD(input, psf, iterations1, 1, tv);
			imstep1stack.addSlice("", imstep1.getStack().getProcessor(1));

			ImagePlus imstep1plus = new ImagePlus("", imstep1stack);
			dealWithTimePointFrame(f, imstep1plus);
			imstep1plus = null;
		}
		SignalCollector.clear();

	}
	
	protected void dealWithTimePointFrame(int f, ImagePlus cum) {
		ImageStack imsReconstruction;			
		if (f == 0) {
			imsReconstruction = new ImageStack(cum.getWidth(), cum.getHeight());
			imsReconstruction.addSlice(cum.getProcessor());
			impReconstruction = new ImagePlus("RL result", imsReconstruction);
			impReconstruction.show();
			Apply_LUT.applyLUT_redhot(impReconstruction);
		}
		else {
			imsReconstruction = impReconstruction.getImageStack();
			imsReconstruction.addSlice(cum.getProcessor());
			impReconstruction.setStack(imsReconstruction);
			if (impReconstruction.getSlice() >= impReconstruction.getNSlices()-1)
				impReconstruction.setSlice(impReconstruction.getNSlices());
		}
	}
	private ImagePlus RLD(ImagePlus imp, ImagePlus psfraw, int iterations, float scale, double tv) {
		RealSignal psfd;

		if (scale != 1) {
			int pw = psfraw.getWidth();
			int ph = psfraw.getHeight();
			float[] scaledpsf = (float[]) psfraw.getProcessor().convertToFloatProcessor().getPixels();
			for (int pl = 0; pl < scaledpsf.length; pl++)
				scaledpsf[pl] = (float) Math.pow(scaledpsf[pl], scale);
			ImageStack psfi = new ImageStack(pw, ph);
			psfi.addSlice("", scaledpsf);
			ImagePlus psfra = new ImagePlus("psf", psfi);
			psfd = build(psfra);
		} else {
			psfd = build(psfraw);
		}
//		if (tv == 0) {
//			RichardsonLucy rl = new RichardsonLucy(iterations);
//		} else {
//			RichardsonLucyTV rl = new RichardsonLucyTV(iterations, tv);
//		}
		RichardsonLucyTV rl = new RichardsonLucyTV(iterations, tv*1E-5);
		RealSignal y = build(imp);
		RealSignal result = rl.run(y, psfd);
		ImagePlus resultplus = build(result);
		SignalCollector.free(result);
		SignalCollector.free(psfd);
		SignalCollector.free(y);		
		SignalCollector.clear();
		return resultplus;
	}

	private RealSignal build(ImagePlus imp) {
		if (imp == null)
			return null;
		int nx = imp.getWidth();
		int ny = imp.getHeight();
		int nz = imp.getStackSize();
		RealSignal signal = new RealSignal("ij-" + imp.getTitle(), nx, ny, nz);
		for (int k = 0; k < nz; k++) {
			ImageProcessor ip = imp.getStack().getProcessor(k + 1).convertToFloat();
			signal.setXY(k, (float[]) ip.getPixels());
		}
		return signal;
	}

	private ImagePlus build(RealSignal signal) {
		if (signal == null)
			return null;
		ImageStack stack = new ImageStack(signal.nx, signal.ny);
		for (int k = 0; k < signal.nz; k++) {
			ImageProcessor ip = new FloatProcessor(signal.nx, signal.ny, signal.getXY(k));
			stack.addSlice(ip);
		}
		return new ImagePlus("", stack);
	}

	public static void main(String[] args) {

		Class<?> clazz = SACD_RL.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(),
				url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);
		new ImageJ();
		ImagePlus image = IJ.openImage();
//		ImagePlus psf = IJ.openImage();
		image.show();
//		psf.show();
		IJ.runPlugIn(clazz.getName(), "");
	}

}