// Conditions of use: You are free to use this software for research or
// educational purposes. In addition, we expect you to include adequate
// citations and acknowledgments whenever you present or publish results that
// are based on it.
//% *********************************************************************************
//% It is a part of publication:
//% Weisong Zhao et al. Enhanced detection of fluorescence fluctuation for  
//% high-throughput super-resolution imaging, Nature Photonics (2023).
//% https://doi.org/10.1038/s41566-023-01234-9
//% *********************************************************************************
//%    Copyright 2019~2023 Weisong Zhao et al.
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

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JDialog;

import Jfft.FloatFFT_2D;
import deconvolutionSACD.algorithm.RichardsonLucy;
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

public class SACD_Analyze extends JDialog implements PlugIn {
	private static int order = 2;
	private static int iterations1 = 10;
	private static int iterations2 = 10;
	private static double tv = 0;
	private static int skip = 20;
	private static float scale = 2;
	private static int N = 2;
	private static float subfactor = (float) 0.8;
	private static int rollfactor = 20;
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

		String titlePSF = Prefs.get("SACD.titlePSF", titles[0]);
		int psfChoice = 0;
		for (int i = 0; i < wList.length; i++) {
			if (titlePSF.equals(titles[i])) {
				psfChoice = i;
				break;
			}
		}
		String titlePSF2 = Prefs.get("SACD.titlePSF2", titles[0]);
		int psfChoice2 = 0;
		for (int i = 0; i < wList.length; i++) {
			if (titlePSF2.equals(titles[i])) {
				psfChoice2 = i;
				break;
			}
		}

		GenericDialog gd = new GenericDialog("SACD: Faster fluctuation image reconstruction");

		gd.addChoice("Image sequence", titles, titles[imageChoice]);
		gd.addChoice("PSF under original pixel size", titles, titles[psfChoice]);
		gd.addChoice("PSF under interpolated pixel size (/N)", titles, titles[psfChoice2]);

		gd.addMessage("SACD core parameters:", new Font("SansSerif", Font.BOLD, 14), new Color(0, 100, 255));
		gd.addNumericField("Stack per SR frame", skip, 0, 5, "20~50 frames");
		gd.addNumericField("1st iterations (10)", iterations1, 0, 5, "times");
		gd.addNumericField("Fourier interpolation (N)", N, 0, 3, "times");
		gd.addNumericField("2nd iterations (10)", iterations2, 0, 5, "times");
		gd.addMessage("__________________________________________________________");
		gd.addMessage("Advanced settings:", new Font("SansSerif", Font.BOLD, 14), new Color(0, 100, 255));
		gd.addNumericField("Order", order, 0, 3, "2 (1~4)");
		gd.addNumericField("Scale of PSF", scale, 1, 3, "2 (1~4)");
		gd.addNumericField("Subtract factor", subfactor, 1, 5, "0.8 (or 0.5)");
		gd.addNumericField("TV weight (value x 1e-5)", tv, 2);
		gd.addNumericField("Rolling factor", rollfactor, 0, 5, "stack (1~stack) frames");
		gd.addHelp("https://github.com/WeisongZhao/SACDj");
//		boolean ifsub = Prefs.get("SACD.sub", false);
//
//		String[] cbgl2 = new String[] { "Subtract mean value", };
//		boolean[] cbgd2 = new boolean[] { ifsub };
//		gd.addCheckboxGroup(1, 2, cbgl2, cbgd2);

		gd.showDialog();
		if (gd.wasCanceled())
			return;
		// Get parameters

		skip = (int) gd.getNextNumber();
		iterations1 = (int) gd.getNextNumber();
		N = (int) gd.getNextNumber();
		iterations2 = (int) gd.getNextNumber();
		order = (int) gd.getNextNumber();
		scale = (float) gd.getNextNumber();
		subfactor = (float) gd.getNextNumber();
		tv = gd.getNextNumber();
		rollfactor = (int) gd.getNextNumber();
		ImagePlus impY = WindowManager.getImage(wList[gd.getNextChoiceIndex()]);
		ImagePlus impA = WindowManager.getImage(wList[gd.getNextChoiceIndex()]);
		ImagePlus impA2 = WindowManager.getImage(wList[gd.getNextChoiceIndex()]);
//		ifsub = gd.getNextBoolean();
//		Prefs.set("SACD.sub", ifsub);
//		gd.addMessage("Note");
		if (order > 4) {
			IJ.error("Warnning! Higher cumulants than 4th order are usually ugly");
			return;
		}
		if (!showDialog())
			return;

		SACD_recon(impY, impA, impA2, skip, iterations1, tv, N, order, scale, iterations2, subfactor, rollfactor);
	}

	private boolean showDialog() {

		return true;
	}

//	public void SACD_recon(ImagePlus imp, ImagePlus psf, ImagePlus psf2, int skip, int iterations1, int N, int order,
//			float scale, int iterations2, float subfactor, int rollfactor) {
//		int w = imp.getWidth(), h = imp.getHeight(), t = imp.getStackSize();
//		ImageStack imstack = imp.getStack();
//		skip = Math.min(t, skip);
//		ImageStack SACDstack = new ImageStack(w * N, h * N);
//		int frame = t / skip;
//		rollfactor = Math.min(rollfactor, skip);
//		for (int f = 0; f < frame * skip; f = f + rollfactor) {
//
//			ImagePlus SACD;
//			ImageStack imstep1stack = new ImageStack(w, h);
//
//			for (int sk = f; sk < f + skip; sk++) {
//				IJ.showStatus("1st Deconvolution");
//				IJ.showProgress(sk - f, skip);
//				ImageStack inputstack = new ImageStack(w, h);
//				inputstack.addSlice("", imstack.getProcessor(sk + 1));
//				ImagePlus input = new ImagePlus("", inputstack);
//				ImagePlus imstep1 = RLD(input, psf, iterations1, 1);
//				imstep1stack.addSlice("", imstep1.getStack().getProcessor(1));
//			}
//			ImagePlus imstep1plus = new ImagePlus("", imstep1stack);
//
//			if (N != 1) {
//				IJ.showStatus("Fourier Interpolation");
//				ImagePlus implarge = FourierInterpolation(imstep1plus, N);
//				ImagePlus cum = Cumulant(implarge, order, subfactor);
//				IJ.showStatus("2nd Deconvolution");
//				SACD = RLD(cum, psf2, iterations2, scale);
//			} else {
//				ImagePlus cum = Cumulant(imstep1plus, order, subfactor);
//				IJ.showStatus("2nd Deconvolution");
//				SACD = RLD(cum, psf, iterations2, scale);
//			}
//			SACDstack.addSlice("", SACD.getProcessor());
//		}
//		ImagePlus SACDshow = new ImagePlus("SACD result", SACDstack);
//		SACDshow.show();
//		IJ.showStatus("2nd Deconvolution");
//	}
	public void SACD_recon(ImagePlus imp, ImagePlus psf, ImagePlus psf2, int skip, int iterations1, double tv, int N,
			int order, float scale, int iterations2, float subfactor, int rollfactor) {
		int w = imp.getWidth(), h = imp.getHeight(), t = imp.getStackSize();
		ImageStack imstack = imp.getStack();
		skip = Math.min(t,skip);	
		rollfactor = Math.min(rollfactor,skip);
		int frame = (t - skip)/ rollfactor + 1;
		ImagePlus SACD;
		for (int f = 0; f < frame * rollfactor; f = f + rollfactor) {
			ImageStack imstep1stack = new ImageStack(w, h);
			
			for (int sk = f; sk < f + skip; sk++) {
				IJ.showStatus("1st Deconvolution");
				IJ.showProgress(sk - f, skip);
				ImageStack inputstack = new ImageStack(w, h);
				inputstack.addSlice("", imstack.getProcessor(sk + 1));
				ImagePlus input = new ImagePlus("", inputstack);
				ImagePlus imstep1 = RLD(input, psf, iterations1, 1);
				imstep1stack.addSlice("", imstep1.getStack().getProcessor(1));
			}
			ImagePlus imstep1plus = new ImagePlus("", imstep1stack);

			if (N != 1) {
				IJ.showStatus("Fourier Interpolation");
				ImagePlus implarge = FourierInterpolation(imstep1plus, N);
				ImagePlus cum = Cumulant(implarge, order, subfactor);
				IJ.showStatus("2nd Deconvolution");
				SACD = RLDTV(cum, psf2, iterations2, scale, tv);
			} else {
				ImagePlus cum = Cumulant(imstep1plus, order, subfactor);
				IJ.showStatus("2nd Deconvolution");
				SACD = RLDTV(cum, psf, iterations2, scale, tv);
			}
			dealWithTimePointFrame(f, SACD);
		}
		SignalCollector.clear();
	}
	protected void dealWithTimePointFrame(int f, ImagePlus cum) {
		ImageStack imsReconstruction;			
		if (f == 0) {
			imsReconstruction = new ImageStack(cum.getWidth(), cum.getHeight());
			imsReconstruction.addSlice(cum.getProcessor());
			impReconstruction = new ImagePlus("SACD result", imsReconstruction);
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
	private ImagePlus RLD(ImagePlus imp, ImagePlus psfraw, int iterations, float scale) {
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
		RichardsonLucy rl = new RichardsonLucy(iterations);
		RealSignal y = build(imp);
		RealSignal result = rl.run(y, psfd);
		ImagePlus resultplus = build(result);
//		resultplus.show();
		SignalCollector.free(result);
		SignalCollector.free(psfd);
		SignalCollector.free(y);
		SignalCollector.clear();
		return resultplus;
	}

	private ImagePlus RLDTV(ImagePlus imp, ImagePlus psfraw, int iterations, float scale, double tv) {
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
//		System.out.print("TVVVVVVVVVVVVVVVVVVVVVVVVVVV");
		RichardsonLucyTV rl = new RichardsonLucyTV(iterations, tv * 1E-5);
		RealSignal y = build(imp);
		RealSignal result = rl.run(y, psfd);
		ImagePlus resultplus = build(result);
//		resultplus.show();
		SignalCollector.free(result);
		SignalCollector.free(psfd);
		SignalCollector.free(y);
		SignalCollector.clear();
		return resultplus;
	}

	// Fourier Interpolation
	public static ImagePlus FourierInterpolation(ImagePlus imp, int N) {
		int w = imp.getWidth(), h = imp.getHeight(), d = imp.getImageStackSize();
		ImageStack stackA = imp.getStack();
		float[][] dataAin = new float[d][w * h];
		for (int i = 0; i < d; i++) {
			dataAin[i] = (float[]) stackA.getProcessor(i + 1).convertToFloat().getPixels();
		}
		ImageStack result = new ImageStack(N * w, N * h);
		for (int z = 0; z < d; z++) {
			float[] a = new float[2 * w * h];
			for (int k2 = 0; k2 < h; k2++) {
				for (int k1 = 0; k1 < w; k1++) {
					a[k2 * 2 * w + 2 * k1] = dataAin[z][k2 * w + k1];
					a[k2 * 2 * w + 2 * k1 + 1] = dataAin[z][k2 * w + k1];
				}
			}
			int s = w;
			if (w < h)
				s = h;
			if (w > h)
				s = w;
			if (w == h)
				s = w;
			float[] b = new float[N * N * s * s];
			float[] bl;
			float[] largea = padScale(a, w, h);

			FloatFFT_2D FFT_J = new FloatFFT_2D(s, s);

			FFT_J.complexForward(largea);

			bl = pad2D(largea, s, s, N);

			FloatFFT_2D FFT_Jinverse = new FloatFFT_2D(N * s, N * s);

			FFT_Jinverse.complexInverse(bl, false);

			for (int i = 0; i < bl.length / 2; i++) {
				b[i] = (float) sqrt(abs(bl[i * 2] * bl[i * 2] + bl[i * 2 + 1] * bl[i * 2 + 1]));
			}

			result.addSlice("", cutScale(b, w, h, N));
		}
		ImagePlus image = new ImagePlus("Fourier interpolated", result);
//		image.show();
		return image;
	}

	// Complex signal pad
	public static float[] padScale(float[] input, int w, int h) {
		int s = w;
		if (w < h)
			s = h;
		if (w > h)
			s = w;
		if (w == h)
			s = w;
		float[] scale = new float[2 * s * s];
		if (w < h) {
			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w; j++) {
					scale[i * 2 * h + j * 2] = input[i * 2 * w + j * 2];
					scale[i * 2 * h + j * 2 + 1] = input[i * 2 * w + j * 2 + 1];
				}
			}
		} else if (w > h) {
			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w; j++) {
					scale[i * 2 * w + j * 2] = input[i * 2 * w + j * 2];
					scale[i * 2 * w + j * 2 + 1] = input[i * 2 * w + j * 2 + 1];
				}
			}
		} else {
			scale = input;
		}
		return scale;
	}

	// Real signal cut
	public static float[] cutScale(float[] input, int w, int h, int N) {
		//
		int s = N * w;
		if (w < h)
			s = N * h;
		if (w > h)
			s = N * w;
		if (w == h)
			s = N * w;
		float[] small = new float[N * N * w * h];
		if (w == h) {
			small = input;
		} else {
			for (int i = 0; i < N * h; i++) {
				for (int j = 0; j < N * w; j++) {
					small[i * N * w + j] = input[i * s + j];
				}
			}
		}
		return small;
	}

	public static float[] pad2D(float[] input, int w, int h, int N) {

		int lx = N * w;
		int ly = N * h;

		int ow = w / 2;
		int oh = h / 2;

		if (lx == w)
			if (ly == h)
				return input;

		float[] large = new float[2 * N * N * w * h];
		// first
		for (int i = 0; i < oh; i++) {
			for (int j = 0; j < ow; j++) {
				large[j * 2 + i * lx * 2] = input[j * 2 + i * w * 2];
				large[j * 2 + i * lx * 2 + 1] = input[j * 2 + i * w * 2 + 1];
			}
		}
		// second
		for (int i = 0; i < oh; i++) {
			for (int j = ow; j < w; j++) {
				large[j * 2 + i * lx * 2 + (N - 1) * w * 2] = input[j * 2 + i * w * 2];
				large[j * 2 + i * lx * 2 + (N - 1) * w * 2 + 1] = input[j * 2 + i * w * 2 + 1];
			}
		}
		// third
		for (int i = oh; i < h; i++) {
			for (int j = 0; j < ow; j++) {
				large[j * 2 + i * lx * 2 + N * w * 2 * (N - 1) * h] = input[j * 2 + i * w * 2];
				large[j * 2 + i * lx * 2 + N * w * 2 * (N - 1) * h + 1] = input[j * 2 + i * w * 2 + 1];
			}
		}

		for (int i = oh; i < h; i++) {
			for (int j = ow; j < w; j++) {
				large[j * 2 + i * lx * 2 + (N) * w * 2 * (N - 1) * h + (N - 1) * w * 2] = input[j * 2 + i * w * 2];
				large[j * 2 + i * lx * 2 + (N) * w * 2 * (N - 1) * h + (N - 1) * w * 2 + 1] = input[j * 2 + i * w * 2
						+ 1];
			}
		}
		return large;
	}

	// -------------------------------------------------Cumulant-----------------------------------------------------
	private ImagePlus Cumulant(ImagePlus stack, int order, float subfactor) {
		int w = stack.getWidth(), h = stack.getHeight(), d = stack.getStackSize();
		RealSignal raw = build(stack);
		float[] Cum = new float[w * h];
		// Cumplus.flush();

		if (order != 1) {
			for (int xy = 0; xy < w * h; xy++) {
				float mean = 0;
				for (int t = 0; t < d; t++)
					mean += raw.data[t][xy] / d;
				for (int t = 0; t < d; t++) {
					raw.data[t][xy] -= subfactor * mean;
					raw.data[t][xy] = abs(raw.data[t][xy]);
				}
			}
		}

		IJ.showStatus("Cumulant calculation");
		float normalize = 0;
		for (int i = 0; i < w * h; i++) {
			double meanorder = 0;
			double twoorder12 = 0;
			double twoorder23 = 0;
			double twoorder13 = 0;
			double twoorder14 = 0;
			double twoorder24 = 0;
			double twoorder34 = 0;
			double threeorder = 0;
			double fourorder = 0;
			double finalfourorder = 0;
			for (int z = 0; z < d - order + 1; z++) {

				if (order == 2)
					twoorder12 += raw.data[z][i] * raw.data[z + 1][i];
				else if (order == 3)
					threeorder += raw.data[z][i] * raw.data[z + 1][i] * raw.data[z + 2][i];
				else if (order == 4) {
					twoorder12 += raw.data[z][i] * raw.data[z + 1][i];
					twoorder23 += raw.data[z + 1][i] * raw.data[z + 2][i];
					twoorder13 += raw.data[z][i] * raw.data[z + 2][i];
					twoorder14 += raw.data[z][i] * raw.data[z + 3][i];
					twoorder24 += raw.data[z + 1][i] * raw.data[z + 3][i];
					twoorder34 += raw.data[z + 2][i] * raw.data[z + 3][i];
					fourorder += raw.data[z][i] * raw.data[z + 1][i] * raw.data[z + 2][i] * raw.data[z + 3][i];
					finalfourorder = fourorder - twoorder14 * twoorder23 - twoorder13 * twoorder24
							- twoorder12 * twoorder34;
				} else if (order == 1)
					meanorder += raw.data[z][i];
			}

			if (order == 2)
				Cum[i] = (float) abs((twoorder12 / (d - order + 1)));
			else if (order == 3)
				Cum[i] = (float) abs(threeorder / (d - order + 1));
			else if (order == 4)
				Cum[i] = (float) abs(finalfourorder / (d - order + 1));
			else if (order > 4) {
				IJ.error("Higher cumulants than 4th order are usually ugly");
			} else if (order == 1) {
				Cum[i] = (float) (meanorder / (d - order + 1));
			}
			if (normalize < Cum[i])
				normalize = Cum[i];
			// if (i % 1000 == 0) {
			IJ.showProgress(i, w * h);
			// }
		}
		for (int n = 0; n < w * h; n++)
			Cum[n] = Cum[n] / normalize;
		IJ.showStatus("");
		ImageStack result = new ImageStack(w, h);
		result.addSlice("", Cum);
		ImagePlus image = new ImagePlus("Cumulant result", result);
//		image.show();
		SignalCollector.free(raw);
		return image;
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

		Class<?> clazz = SACD_Analyze.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(),
				url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);
		new ImageJ();
		ImagePlus image = IJ.openImage();
		ImagePlus psf = IJ.openImage();
		ImagePlus psf2 = IJ.openImage();
		image.show();
		psf.show();
		psf2.show();
		IJ.runPlugIn(clazz.getName(), "");
	}

}