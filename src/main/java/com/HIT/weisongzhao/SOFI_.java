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
import static java.lang.Math.sqrt;

import javax.swing.JDialog;

import Jfft.FloatFFT_2D;
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

public class SOFI_ extends JDialog implements PlugIn {
	private static int order = 2;
	private static int skip = 500;
	private static int N = 1;
	private static int rollfactor = skip;
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

		String titleImage = Prefs.get("SOFI.titleImage", titles[0]);
		int imageChoice = 0;
		for (int i = 0; i < wList.length; i++) {
			if (titleImage.equals(titles[i])) {
				imageChoice = i;
				break;
			}
		}

		GenericDialog gd = new GenericDialog("SOFI: Fluctuation image analyse");
		gd.addChoice("Image sequence", titles, titles[imageChoice]);
		gd.addNumericField("Stack per SR frame", skip, 0, 5, "500~800 frames");
		gd.addNumericField("Fourier interpolation", N, 0, 3, "times");
		gd.addNumericField("Order", order, 0, 3, "(1~4)");
		gd.addNumericField("Rolling factor", rollfactor, 0, 5, "stack (1~stack)");
		gd.addHelp("https://github.com/WeisongZhao/SACDj");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		// Get parameters

		skip = (int) gd.getNextNumber();
		N = (int) gd.getNextNumber();
		order = (int) gd.getNextNumber();
		if (order > 4) {
			IJ.error("Warnning! Higher cumulants than 4th order are usually ugly");
			return;
		} 
		ImagePlus impY = WindowManager.getImage(wList[gd.getNextChoiceIndex()]);

//		gd.addMessage("Note");
		if (!showDialog())
			return;
		SOFI_recon(impY, skip, N, order, rollfactor);
	}

	private boolean showDialog() {

		return true;
	}

//	public void SOFI_recon(ImagePlus imp, int skip, int N, int order, int rollfactor) {
//		int w = imp.getWidth(), h = imp.getHeight(), t = imp.getStackSize();
//		ImageStack imstack = imp.getStack();
//		ImageStack SOFIstack = new ImageStack(w * N, h * N);
//		skip = Math.min(t,skip);
//		int frame = imp.getStackSize() / skip;
//		rollfactor = Math.min(rollfactor,skip);
//		for (int f = 0; f < frame * skip; f = f + rollfactor) {
//
//			ImageStack imstep1stack = new ImageStack(w, h);
//			ImageStack inputstack = new ImageStack(w, h);
//			ImagePlus cum;
//			for (int sk = f; sk < f + skip; sk++) {
//				inputstack.addSlice("", imstack.getProcessor(sk + 1));
//			}
//			ImagePlus imstep1plus = new ImagePlus("", inputstack);
//
//			if (N != 1) {
//				IJ.showStatus("Fourier Interpolation");
//				ImagePlus implarge = FourierInterpolation(imstep1plus, N);
//				cum = Cumulant(implarge, order);
//			} else {
//				cum = Cumulant(imstep1plus, order);
//			}
//			SOFIstack.addSlice("", cum.getProcessor());
//		}
//		ImagePlus SOFIshow = new ImagePlus("SOFI result", SOFIstack);
//		SOFIshow.show();
//	}

	public void SOFI_recon(ImagePlus imp, int skip, int N, int order, int rollfactor) {
		int w = imp.getWidth(), h = imp.getHeight(), t = imp.getStackSize();
		ImageStack imstack = imp.getStack();
		skip = Math.min(t,skip);
		int frame = imp.getStackSize() / skip;
		rollfactor = Math.min(rollfactor,skip);
		ImageStack inputstack = new ImageStack(w, h);
		ImagePlus imstep1plus = new ImagePlus("", inputstack);
		for (int f = 0; f < frame * skip; f = f + rollfactor) {
			ImagePlus cum;
			for (int sk = f; sk < f + skip; sk++) {
				inputstack.addSlice("", imstack.getProcessor(sk + 1));
			}
		
			if (N != 1) {
				IJ.showStatus("Fourier Interpolation");
				ImagePlus implarge = FourierInterpolation(imstep1plus, N);
				cum = Cumulant(implarge, order);
			} else {
				cum = Cumulant(imstep1plus, order);
			}
			
			ImageStack imsReconstruction;			
			if (f == 0) {
	            imsReconstruction = new ImageStack(cum.getWidth(), cum.getHeight());
	            imsReconstruction.addSlice(cum.getProcessor());
	            impReconstruction = new ImagePlus("SOFI result", imsReconstruction);
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
	private ImagePlus Cumulant(ImagePlus stack, int order) {
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
					raw.data[t][xy] -= 1 * mean;
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
				return null;
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

		Class<?> clazz = SOFI_.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(),
				url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);
		new ImageJ();
		ImagePlus image = IJ.openImage();
		image.show();
		IJ.runPlugIn(clazz.getName(), "");
	}

}