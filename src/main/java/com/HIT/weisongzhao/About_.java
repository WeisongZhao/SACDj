package com.HIT.weisongzhao;

import javax.swing.JDialog
;

import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

public class About_ extends JDialog implements PlugIn {
	private String defaultMessage = "(c) 2020 	HIT";
	private WalkBar walk = new WalkBar(this.defaultMessage, true, false, true);

	@Override
	public void run(String arg) {
		this.walk.fillAbout("SACD Reconstruction", " 12/04/2020", "Faster super-resolution fluctuation imaging<br>v1.1.3",
				"School of Instrumentation Science and Engineering<br/>Harbin Institute of Technology",
				"Weisong Zhao (zhaoweisong950713@163.com)", "2020",
				"<p style=\"text-align:left\">"
				+ "<b>Publications:</b>"
				+ "<br> Weisong, Zhao, et al. \"[1]. Weisong Zhao, et al. SACD. (2021)."
				+ "<br><br><b>References:</b>"
				+ "<br>[1] D. Sage, L. Donati, F. Soulez, D. Fortun, G. Schmit, A. Seitz, R. Guiet, C. Vonesch, and M. Unser, \"DeconvolutionLab2: An open-source software for deconvolution microscopy,\" Methods vol. 115, no. 28, 2017."
				+ "<br>[2] http://bigwww.epfl.ch/algorithms/psfgenerator/"
				+ "<br>[3] https://github.com/wendykierp/JTransforms"				
				+ "<br><br><b>Acknowledgements:</b><br>This plugin is built on the top of DeconvolutionLab2 [1], PSF_Generator [2], and JTransforms [3] "
				+ "<br><br><b>Open source:</b><br>https://github.com/WeisongZhao/SACDj"
				+ "");
		
		this.walk.showAbout_SACD();

	}

	public static void main(String[] args) {

		Class<?> clazz = About_.class;

		// start ImageJ
		new ImageJ();

		IJ.runPlugIn(clazz.getName(), "");
	}

}