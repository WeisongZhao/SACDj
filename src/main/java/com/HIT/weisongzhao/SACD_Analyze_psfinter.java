/* 
* Conditions of use: You are free to use this software for research or


* educational purposes. In addition, we expect you to include adequate
* citations and acknowledgments whenever you present or publish results that
* are based on it.
* 
* Reference: [1]. Weisong Zhao, et al. "SACD (2021).
*/

/*
 * Copyright 2020 Weisong Zhao.
 * 
 * This file is part of SACD Analyze plugin (SACD).
 * 
 * SACD is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * SACD is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * SACD. If not, see <http://www.gnu.org/licenses/>.
 */

package com.HIT.weisongzhao;

import static java.lang.Math.abs;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JDialog;

import deconvolutionSACD.algorithm.RichardsonLucy;
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

public class SACD_Analyze_psfinter extends JDialog implements PlugIn {
	private static int iterations1 = 30;
	private static int iterations2 = 60;
	private static int skip = 20;
	private static int N = 1;
	private static double NA = 1.4;
	private static double lambda = 561;
	private static double lateralres = 65;
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

		gd.addMessage("For PSF:", new Font("SansSerif", Font.BOLD, 14), new Color(0, 100, 255));
		gd.addNumericField("NA", NA, 1);
		gd.addNumericField("Wave length (nm)", lambda, 0);
		gd.addNumericField("Pixel size (nm)", lateralres, 2);

		gd.addMessage("SACD core parameters:", new Font("SansSerif", Font.BOLD, 14), new Color(0, 100, 255));

		gd.addNumericField("Stack per SR frame", skip, 0, 5, "20~50 frames");
		gd.addNumericField("1st iterations (30)", iterations1, 0, 5, "times");
		gd.addNumericField("Fourier interpolation", N, 0, 3, "times");
		gd.addNumericField("2nd iterations (60)", iterations2, 0, 5, "times");

//		boolean ifsub = Prefs.get("SACD.sub", false);
//
//		String[] cbgl2 = new String[] { "Subtract mean value", };
//		boolean[] cbgd2 = new boolean[] { ifsub };
//		gd.addCheckboxGroup(1, 2, cbgl2, cbgd2);

		gd.showDialog();
		if (gd.wasCanceled())
			return;
		// Get parameters
		NA = gd.getNextNumber();
		lambda = gd.getNextNumber();
		lateralres = gd.getNextNumber();
		skip = (int) gd.getNextNumber();
		iterations1 = (int) gd.getNextNumber();
		N = (int) gd.getNextNumber();
		iterations2 = (int) gd.getNextNumber();
		ImagePlus impY = WindowManager.getImage(wList[gd.getNextChoiceIndex()]);
		if (!showDialog())
			return;
		SACD_recon(impY, NA, lambda, lateralres, skip, iterations1, N, 2, 2, iterations2, (float) 0.5, skip);
	}

	private boolean showDialog() {

		return true;
	}

//	public void SACD_recon(ImagePlus imp, double NA, double lambda, double resLateral, int skip, int iterations1, int N,
//			int order, float scale, int iterations2, float subfactor, int rollfactor) {
//
//		ImagePlus psf = SACD_BornWolf.CreatPSF(NA, lambda, resLateral);
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
//				ImagePlus implarge = SACD_BornWolf.FourierInterpolation(imstep1plus, N);
//				ImagePlus cum = Cumulant(implarge, order, subfactor);
//				IJ.showStatus("2nd Deconvolution");
//				ImagePlus psf2 = SACD_BornWolf.CreatPSF(NA, lambda, resLateral / N);
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
	public void SACD_recon(ImagePlus imp, double NA, double lambda, double resLateral, int skip, int iterations1, int N,
			int order, float scale, int iterations2, float subfactor, int rollfactor) {

		ImagePlus psf = SACD_BornWolf.CreatPSF(NA, lambda, resLateral);
		int w = imp.getWidth(), h = imp.getHeight(), t = imp.getStackSize();
		ImageStack imstack = imp.getStack();
		skip = Math.min(t, skip);
		ImageStack SACDstack = new ImageStack(w * N, h * N);
		int frame = t / skip;
		rollfactor = Math.min(rollfactor, skip);
		for (int f = 0; f < frame * skip; f = f + rollfactor) {

			ImagePlus SACD;
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
				ImagePlus implarge = SACD_BornWolf.FourierInterpolation(imstep1plus, N);
				ImagePlus cum = Cumulant(implarge, order, subfactor);
				IJ.showStatus("2nd Deconvolution");
				ImagePlus psf2 = SACD_BornWolf.CreatPSF(NA, lambda, resLateral / N);
				SACD = RLD(cum, psf2, iterations2, scale);
			} else {
				ImagePlus cum = Cumulant(imstep1plus, order, subfactor);
				IJ.showStatus("2nd Deconvolution");
				SACD = RLD(cum, psf, iterations2, scale);
			}
			ImageStack imsReconstruction;
			if (f == 0) {
				imsReconstruction = new ImageStack(SACD.getWidth(), SACD.getHeight());
				imsReconstruction.addSlice(SACD.getProcessor());
				impReconstruction = new ImagePlus("SOFI result", imsReconstruction);
				impReconstruction.show();
				Apply_LUT.applyLUT_redhot(impReconstruction);
			} else {
				imsReconstruction = impReconstruction.getImageStack();
				imsReconstruction.addSlice(SACD.getProcessor());
				impReconstruction.setStack(imsReconstruction);
				if (impReconstruction.getSlice() >= impReconstruction.getNSlices() - 1)
					impReconstruction.setSlice(impReconstruction.getNSlices());
			}
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
		return resultplus;
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

		Class<?> clazz = SACD_Analyze_psfinter.class;
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