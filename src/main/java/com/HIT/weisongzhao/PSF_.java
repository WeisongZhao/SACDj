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

import javax.swing.JDialog;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class PSF_ extends JDialog implements PlugIn {
	private static double NA = 1.4;
	private static double lambda = 561;
	private static double lateralres = 65;

	@Override
	public void run(String arg) {

		if (IJ.versionLessThan("1.46j"))
			return;
	
		GenericDialog gd = new GenericDialog("PSF - Born&Wolf");

		gd.addNumericField("NA", NA, 2);
		gd.addNumericField("Wave length (nm)", lambda, 0);
		gd.addNumericField("Pixel size (nm)", lateralres, 2);
		gd.addHelp("https://github.com/WeisongZhao/SACDj");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		// Get parameters
		NA = gd.getNextNumber();
		lambda = gd.getNextNumber();
		lateralres = gd.getNextNumber();

		if (!showDialog())
			return;
		
		ImagePlus psf = SACD_BornWolf.CreatPSF(NA, lambda, lateralres);
		psf.show();
	}

	private boolean showDialog() {

		return true;
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