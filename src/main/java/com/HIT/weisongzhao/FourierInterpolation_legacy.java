package com.HIT.weisongzhao;

import java.util.concurrent.Callable;

import com.HIT.weisongzhao.Algorithm_new;

import ij.ImagePlus;
import ij.ImageStack;
import signalSACD.ComplexSignal;
import signalSACD.Operations;
import signalSACD.RealSignal;
import signalSACD.SignalCollector;

public class FourierInterpolation_legacy extends Algorithm_new implements Callable<RealSignal> {

	public FourierInterpolation_legacy(int N) {
		super();
		this.N = N;
	}

	@Override
	public RealSignal call() {
		int w = y.nx, h = y.ny, d = y.nz;
		RealSignal raw = y.duplicate();
		RealSignal large = new RealSignal(getName(), N * w, N * h, d);
		// for (int z = 0; z < d; z++) {
		RealSignal rawslice = new RealSignal(getName(), w, h, 1);
		ComplexSignal rawslicefft = new ComplexSignal(getName(), w, h, d);
		ComplexSignal largeslicefft = new ComplexSignal(getName(), N * w, N * h, d);
		RealSignal largeslice = new RealSignal(getName(), N * w, N * h, d);
		
		RealSignal rawA = Operations.circularShift(raw);
		fft.transform(rawA, rawslicefft);
		System.out.print(large.data[0].length);
		largeslicefft = pad(rawslicefft, 2);

		// System.out.print(largeslicefft);
		// System.out.print(large);
		large = fft.inverse(largeslicefft);
		// large.data[z] = largeslice.data[0];
		// }
		return large;
	}

	private ComplexSignal pad(ComplexSignal input, int N) {

		int nx = input.nx;
		int ny = input.ny;
		int nz = input.nz;

		int lx = N * nx;
		int ly = N * ny;
		int lz = nz;

		if (lx == nx)
			if (ly == ny)
				if (lz == nz)
					return input.duplicate();

		int ox = (lx - nx) / 2;
		int oy = (ly - ny) / 2;

		String name = "pad(" + input.name + ")";
		ComplexSignal large = new ComplexSignal(name, lx, ly, lz);
		for (int k = 0; k < nz; k++) {
			for (int i = 0; i < ny; i++) {
				for (int j = 0; j < nx; j++) {
					large.data[k][(oy + i) * nx + (j + ox) * 2] = input.data[k][(j + i * nx) * 2];
					large.data[k][(oy + i) * nx + (j + ox) * 2 + 1] = input.data[k][(j + i * nx) * 2 + 1];
				}
			}
		}
		System.out.print(large.data[0].length);
		return large;
	}
	
//	public float[] getX(int j, int k) {
//		float line[] = new float[nx];
//		for (int i = 0; i < nx; i++)
//			line[i] = data[k][i + j * nx];
//		return line;
//	}
//
//	public float[] getZ(int i, int j) {
//		float line[] = new float[nz];
//		int index = i + j * nx;
//		for (int k = 0; k < nz; k++)
//			line[k] = data[k][index];
//		return line;
//	}
//
//	public float[] getY(int i, int k) {
//		float line[] = new float[ny];
//		for (int j = 0; j < ny; j++)
//			line[j] = data[k][i + j * nx];
//		return line;
//	}
//	public RealSignal circular() {
//		for (int i = 0; i < nx; i++)
//			for (int j = 0; j < ny; j++)
//				setZ(i, j, rotate(getZ(i, j)));
//		for (int i = 0; i < nx; i++)
//			for (int k = 0; k < nz; k++)
//				setY(i, k, rotate(getY(i, k)));
//		for (int j = 0; j < ny; j++)
//			for (int k = 0; k < nz; k++)
//				setX(j, k, rotate(getX(j, k)));
//		return this;
//	}
//	public float[] rotate(float[] buffer) {
//		int len = buffer.length;
//		if (len <= 1)
//			return buffer;
//		int count = 0;
//		int offset = 0;
//		int start = len / 2;
//		while (count < len) {
//			int index = offset;
//			float tmp = buffer[index];
//			int index2 = (start + index) % len;
//			while (index2 != offset) {
//				buffer[index] = buffer[index2];
//				count++;
//				index = index2;
//				index2 = (start + index) % len;
//			}
//			buffer[index] = tmp;
//			count++;
//			offset++;
//		}
//		return buffer;
//	}
	@Override
	public String getName() {
		return "Fourier-Interpolation";
	}

	@Override
	public String[] getShortnames() {
		return new String[] { "Interpolated" };
	}

	@Override
	public int getComplexityNumberofFFT() {
		return 1 + 7 * N;
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
		return false;
	}

	@Override
	public boolean isWaveletsBased() {
		return false;
	}

	@Override
	public Algorithm_new setParameters(double... params) {
		if (params == null)
			return this;
		if (params.length > 0)
			N = (int) Math.round(params[0]);
		return this;
	}

	@Override
	public double[] getDefaultParameters() {
		return new double[] { 10 };
	}

	@Override
	public double[] getParameters() {
		return new double[] { N };
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