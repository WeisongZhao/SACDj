/*
 * DeconvolutionLab2
 * 
 * Conditions of use: You are free to use this software for research or
 * educational purposes. In addition, we expect you to include adequate
 * citations and acknowledgments whenever you present or publish results that
 * are based on it.
 * 
 * Reference: DeconvolutionLab2: An Open-Source Software for Deconvolution
 * Microscopy D. Sage, L. Donati, F. Soulez, D. Fortun, G. Schmit, A. Seitz,
 * R. Guiet, C. Vonesch, M Unser, Methods of Elsevier, 2017.
 */

/*
 * Copyright 2010-2017 Biomedical Imaging Group at the EPFL.
 * 
 * This file is part of DeconvolutionLab2 (DL2).
 * 
 * DL2 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DL2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DL2. If not, see <http://www.gnu.org/licenses/>.
 */

package deconvolutionSACD.algorithm;

import java.util.concurrent.Callable;

import signalSACD.ComplexSignal;
import signalSACD.Operations;
import signalSACD.RealSignal;
import signalSACD.SignalCollector;

public class RichardsonLucy extends Algorithm implements Callable<RealSignal> {

	public RichardsonLucy(int iterMax) {
		super();
		this.iterMax = iterMax;
	}

	@Override
	public RealSignal call() {
		ComplexSignal H = fft.transform(h);
		ComplexSignal U = new ComplexSignal("RL-U", y.nx, y.ny, y.nz);
		RealSignal p = y.duplicate();
		RealSignal u = y.duplicate();
		RealSignal x = y.duplicate();
		RealSignal y_vector = y.duplicate();
		// For vector acceleration
		RealSignal v_vector = new RealSignal("x", y.nx, y.ny, y.nz);
		RealSignal vv_update = v_vector.duplicate();
		RealSignal x_update = v_vector.duplicate();

		// First iteration !
		fft.transform(x, U);
		U.times(H);
		fft.inverse(U, u);
		Operations.divide(y, u, p);
		fft.transform(p, U);
		U.timesConjugate(H);
		fft.inverse(U, u);
		y_vector.times(u);
		Operations.subtract(y_vector, x, v_vector);
		x = y_vector.duplicate();

		// iteration really start!
		while (!controller.ends(x)) {
			float alpha = 0;
			float alphal = 0;
			float alphau = 0;
			// For vector acceleration
			x_update = y_vector.duplicate();
			fft.transform(x, U);
			U.times(H);
			fft.inverse(U, u);
			Operations.divide(y, u, p);
			fft.transform(p, U);
			U.timesConjugate(H);
			fft.inverse(U, u);
			y_vector.times(u);

			vv_update = v_vector.duplicate();
			Operations.subtract(y_vector, x, v_vector);
			for (int z = 0; z < y.nz; z++) {
				for (int i = 0; i < y.nx * y.ny; i++) {
					alphau += vv_update.data[z][i] * v_vector.data[z][i];
					alphal += vv_update.data[z][i] * vv_update.data[z][i];
				}
			}
			alpha = alphau / (alphal + (float) 1E-6);
			if (alpha < 0)
				alpha = (float) 1E-6;
			if (alpha > 1)
				alpha = 1;
			x = Operations.subtract(y_vector, x_update).times(alpha).plus(y_vector);
		}
		SignalCollector.free(H);
		SignalCollector.free(p);
		SignalCollector.free(u);
		SignalCollector.free(U);
		SignalCollector.free(x_update);
		SignalCollector.free(v_vector);
		SignalCollector.free(y_vector);
		SignalCollector.free(vv_update);
		SignalCollector.free(y);
		return x;
	}

	@Override
	public String getName() {
		return "Richardson-Lucy";
	}

	@Override
	public String[] getShortnames() {
		return new String[] { "RL" };
	}

	@Override
	public int getComplexityNumberofFFT() {
		return 1 + 5 * iterMax;
	}

	@Override
	public double getMemoryFootprintRatio() {
		return 9.0;
	}

	@Override
	public boolean isRegularized() {
		return false;
	}

	@Override
	public boolean isStepControllable() {
		return false;
	}

	@Override
	public boolean isIterative() {
		return true;
	}

	@Override
	public boolean isWaveletsBased() {
		return false;
	}

	@Override
	public Algorithm setParameters(double... params) {
		if (params == null)
			return this;
		if (params.length > 0)
			iterMax = (int) Math.round(params[0]);
		return this;
	}

	@Override
	public double[] getDefaultParameters() {
		return new double[] { 10 };
	}

	@Override
	public double[] getParameters() {
		return new double[] { iterMax };
	}

	@Override
	public double getRegularizationFactor() {
		return 0.0;
	}

	@Override
	public double getStepFactor() {
		return 0;
	}

}
