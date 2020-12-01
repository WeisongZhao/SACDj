package com.HIT.weisongzhao;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class RichardsonLucysimplified {
	private int N;

	public RichardsonLucysimplified() {
		N = 0;
	}

	public double[] run(double[] img, int n, int nh, double[] h, int max_iter) {
		N = n;

		double[] rst = new double[N];
		System.arraycopy(img, 0, rst, 0, N);

		Complex[] x = new Complex[N];
		real2complex(x, rst, n);

		Complex[] psf = new Complex[nh];
		real2complex(psf, h, nh);

		FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
		Complex[] dft_h = fft.transform(psf, TransformType.FORWARD);

		for (int i = 0; i < max_iter; i++) {
			System.out.println("iter: " + i);
			x = fft.transform(x, TransformType.FORWARD);
			ComplexMul(x, dft_h);
			x = fft.transform(x, TransformType.INVERSE);
			FloatDivide(x, img);

			x = fft.transform(x, TransformType.FORWARD);
			ComplexMulConjugate(x, dft_h);
			x = fft.transform(x, TransformType.INVERSE);
			FloatMul(x, rst);
		}
		return rst;
	}

	private void FloatMul(Complex[] x, double[] rst) {
		for (int i = 0; i < N; i++) {
			rst[i] *= x[i].getReal() / N;
			x[i] = new Complex(rst[i], 0);
		}
	}

	private void FloatDivide(Complex[] x, double[] img) {
		for (int i = 0; i < N; i++) {
			if (x[i].getReal() != 0) {
				x[i] = new Complex(img[i] / x[i].getReal() * N, 0);
			} else {
				x[i] = new Complex(0, 0);
			}
		}
	}

	private void ComplexMulConjugate(Complex[] x, Complex[] h) {
		for (int i = 0; i < N; i++) {
			double real1, real2, imag1, imag2;
			real1 = x[i].getReal();
			imag1 = x[i].getImaginary();
			real2 = h[i].getReal();
			imag2 = -h[i].getImaginary();
			x[i] = new Complex(real1 * real2 - imag1 * imag2, real1 * imag2 + real2 * imag1);
		}
	}

	private void ComplexMul(Complex[] x, Complex[] h) {
		for (int i = 0; i < N; i++) {
			double real1, real2, imag1, imag2;
			real1 = x[i].getReal();
			imag1 = x[i].getImaginary();
			real2 = h[i].getReal();
			imag2 = h[i].getImaginary();
			x[i] = new Complex(real1 * real2 - imag1 * imag2, real1 * imag2 + real2 * imag1);
		}
	}

	private void real2complex(Complex[] dst, double[] src, int n) {
		for (int i = 0; i < n; i++) {
			dst[i] = new Complex(src[i]);
		}
	}
}