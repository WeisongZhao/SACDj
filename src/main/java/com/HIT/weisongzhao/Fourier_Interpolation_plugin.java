package com.HIT.weisongzhao;

import javax.swing.JDialog;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;


public class Fourier_Interpolation_plugin extends JDialog implements PlugIn {


	private static int N = 2;
	public void run(String arg) {

		if (IJ.versionLessThan("1.46j"))
			return;
		ImagePlus imp = IJ.getImage();
		if (imp.isComposite() && imp.getNChannels()==imp.getStackSize()) {
			IJ.error("Fourier interpolation", "Composite color images not supported for now");
			return;
		}
		if (!showDialog())
			return;
		imp.startTiming();
		SACD_Analyze.FourierInterpolation(imp, N);
		IJ.showTime(imp, imp.getStartTime(), "", imp.getStackSize());
	}

	private boolean showDialog() {
		GenericDialog gd = new GenericDialog("Fourier interpolation");
		gd.addNumericField("Fourier interpolation", N, 0,3,"times");
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		N = (int) gd.getNextNumber();
		return true;
	}

	

	public static void main(String[] args) {

		Class<?> clazz = Fourier_Interpolation_plugin.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(),
				url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);
		new ImageJ();
		ImagePlus image = IJ.openImage();
		// ImagePlus psf = IJ.openImage();
		image.show();
		// psf.show();
		IJ.runPlugIn(clazz.getName(), "");
	}

}