package com.HIT.weisongzhao;

import javax.swing.JDialog
;

import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

public class About_SACD extends JDialog implements PlugIn {
	private String defaultMessage = "(c) 2020 	HIT";
	private WalkBar_SACD walk = new WalkBar_SACD(this.defaultMessage, true, false, true);

	@Override
	public void run(String arg) {
		this.walk.fillAbout("SACD Reconstruction", "04/09/2022 updated", "Faster super-resolution fluctuation imaging<br>v1.1.3",
				"School of Instrumentation Science and Engineering","Harbin Institute of Technology",
				"Weisong Zhao",
				"<p style=\"text-align:left\">"
				+ "<b>It is a part of publication:</b>"
				+ "<br>Weisong Zhao, et al. High-throughput add-on super-resolution by enhancing detectable fluctuation, <i>Nature Methods</i> (2022)."				
				+ "<br><br><b>Acknowledgements:</b><br>This plugin is for SACD reconstruction (w/o Sparse deconvolution). Please cite PANEL in your publications, if it helps your research."
				+ "<br><br><b>Open source:</b><br>https://github.com/WeisongZhao/SACDj"
				+ "<br><br><b>Used resources:</b>"
				+ "<br>[1] http://bigwww.epfl.ch/deconvolution/deconvolutionlab2"
				+ "<br>[2] http://bigwww.epfl.ch/algorithms/psfgenerator"
				+ "<br>[3] https://github.com/wendykierp/JTransforms"
				+ "");
		
		this.walk.showAbout_SACD();

	}

	public static void main(String[] args) {

		Class<?> clazz = About_SACD.class;

		// start ImageJ
		new ImageJ();

		IJ.runPlugIn(clazz.getName(), "");
	}

}