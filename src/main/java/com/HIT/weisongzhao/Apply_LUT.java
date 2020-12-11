package com.HIT.weisongzhao;

import static com.HIT.weisongzhao.NativeToolsSACD.getLocalFileFromResource;

import java.io.File;
import java.io.IOException;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.LutLoader;
import ij.process.LUT;

public class Apply_LUT {

	public static void applyLUT(ImagePlus imp, String path) {
		File temp = null;
		try {
			temp = getLocalFileFromResource("/" + path);
		} catch (IOException e) {
			IJ.log("Couldn't find resource: " + path);
		}
		if (temp != null) {
			LUT lut = new LutLoader().openLut(temp.getAbsolutePath());
			imp.setLut(lut);
		}
	}

	public static void applyLUT_redhot(ImagePlus imp) {
		applyLUT(imp, "Red_Hot.lut");
	}
}
