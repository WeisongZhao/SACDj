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