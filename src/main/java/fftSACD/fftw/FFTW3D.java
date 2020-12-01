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

package fftSACD.fftw;

import deconvolutionSACDlab.monitor.Monitors;
import fftSACD.AbstractFFT;
import fftSACD.Separability;
import jfftw.complex.nd.Plan;
import signalSACD.ComplexSignal;
import signalSACD.RealSignal;

public class FFTW3D extends AbstractFFT {
	
	private Plan planForwardFFTW = null;
	private Plan planBackwardFFTW = null;

	public FFTW3D() {
		super(Separability.XYZ);
	}

	@Override
	public void init(Monitors monitors, int nx, int ny, int nz) {					
		super.init(monitors, nx, ny, nz);
		int dim[] = new int[] {nz, ny, nx};
		planForwardFFTW = new Plan(dim, Plan.FORWARD, Plan.ESTIMATE | Plan.IN_PLACE | Plan.USE_WISDOM);
		planBackwardFFTW = new Plan(dim, Plan.BACKWARD, Plan.ESTIMATE | Plan.IN_PLACE | Plan.USE_WISDOM);
	}

	@Override
	public void transformInternal(RealSignal x, ComplexSignal X) {
		float interleave[] = x.getInterleaveXYZAtReal();
		planForwardFFTW.transform(interleave);
		X.setInterleaveXYZ(interleave);
	}
	
	@Override
	public void inverseInternal(ComplexSignal X, RealSignal x) {
		float[] interleave = X.getInterleaveXYZ();
		planBackwardFFTW.transform(interleave);
		x.setInterleaveXYZAtReal(interleave);
		x.multiply(1.0/(nx*ny*nz));
	}
	
	@Override
	public String getName() {
		return "FFTW2";
	}
	
	@Override
	public boolean isMultithreadable() {
		return true;
	}

}
