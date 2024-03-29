/*
 * DeconvolutionLab2
 * 
 * Conditions of use: You are free to use this software for research or
 * educational purposes. In addition, we expect you to include adequate
 * citations and acknowledgments whenever you present or publish results that
 * are based on it.
 * 
 * Reference: DeconvolutionLab2: An Open-Source Software for Deconvolution
 * Microscopy D. Sage, L. Donati, F. Soulez, D. Fortun, G. Schmit, A. Seitz,
 * R. Guiet, C. Vonesch, M Unser, Methods of Elsevier, 2017.
 */

/*
 * Copyright 2010-2017 Biomedical Imaging Group at the EPFL.
 * 
 * This file is part of DeconvolutionLab2 (DL2).
 * 
 * DL2 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DL2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DL2. If not, see <http://www.gnu.org/licenses/>.
 */

package deconvolutionSACDlab.module;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import bilib.tools.Files;
import deconvolutionSACD.Command;
import deconvolutionSACDlab.Config;
import deconvolutionSACDlab.Lab;
import deconvolutionSACDlab.Platform;
import deconvolutionSACDlab.dialog.PatternDialog;
import deconvolutionSACDlab.dialog.SyntheticDialog;
import deconvolutionSACDlab.module.dropdownbuttons.ChooseImageDropDownButton;
import deconvolutionSACDlab.module.dropdownbuttons.ShowImageDropDownButton;
import signalSACD.factory.SignalFactory;

public class PSFModule extends AbstractImageSelectionModule {

	private ChooseImageDropDownButton choose;
	private ShowImageDropDownButton show;
	
	public PSFModule() {
		super("psf");
		choose = new ChooseImageDropDownButton("psf", this, "Choose");
		show = new ShowImageDropDownButton("psf", "Check");
		create("PSF", "-psf", show, choose);
	}

	@Override
	public String getCommand() {
		int row = table.getSelectedRow();
		if (row < 0)
			return "";
		String cmd = "-psf " + table.getCell(row, 1) + " " + table.getCell(row, 2);
		if (show != null)
			show.setTitle(table.getCell(row, 0));
		return cmd;
	}

	@Override
	public JPanel buildExpandedPanel() {

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.setLayout(new BorderLayout());
		panel.add(table.getMinimumPane(100, 100), BorderLayout.CENTER);

		// Add drop area
		table.setDropTarget(new LocalDropTarget());
		getCollapsedPanel().setDropTarget(new LocalDropTarget());
		bnTitle.setDropTarget(new LocalDropTarget());
		bnSynopsis.setDropTarget(new LocalDropTarget());
		bnExpand.setDropTarget(new LocalDropTarget());

		Config.registerTable(getName(), "psf", table);
	
		return panel;
	}

	public void update() {
		int row = table.getSelectedRow();
		if (row >= 0) {
			setCommand(getCommand());
			setSynopsis(table.getCell(row, 0));
			Command.buildCommand();
		}
		else {
			setSynopsis("");
			setCommand("<span style='color:#F03030'>Drag the PSF image, here</span>");
		}
		show.setEnabled(table.getRowCount() > 0);
	}

	@Override
	public void edit() {
		int row = table.getSelectedRow();	
		if (row < 0)
			return;
		String source = table.getCell(row, 1).trim().toLowerCase();
		if (source.equals("synthetic")) {
			String name = table.getCell(row, 0).trim();
			for(SignalFactory factory : SignalFactory.getAll()) {
				if (name.equals(factory.getName().trim())) {
					addFromSynthetic(true, "psf");
					return;
				}
			}
		}
		else if (source.equals("directory")) {
			addFromDirectory(table.getCell(row, 2));
		}
		else if (source.equals("file")) {
			addFromFile(table.getCell(row, 2));
		}
		else if (source.equals("platform")) {
			platform();
		}
	}
	
	@Override
	public void close() {
		super.close();
	}

	public class LocalDropTarget extends DropTarget {
		
		@Override
		public void drop(DropTargetDropEvent e) {
			e.acceptDrop(DnDConstants.ACTION_COPY);
			e.getTransferable().getTransferDataFlavors();
			Transferable transferable = e.getTransferable();
			DataFlavor[] flavors = transferable.getTransferDataFlavors();
			for (DataFlavor flavor : flavors) {
				if (flavor.isFlavorJavaFileListType()) {
					try {
						List<File> files = (List<File>) transferable.getTransferData(flavor);
						for (File file : files) {
							if (file.isDirectory()) {
								table.insert(new String[] { file.getName(), "directory", file.getAbsolutePath(), "\u232B" });
								table.setRowSelectionInterval(0, 0);
								update();
							}
							if (file.isFile()) {
								table.insert(new String[] { file.getName(), "file", file.getAbsolutePath(), "\u232B" });
								update();
							}
						}
					}
					catch (UnsupportedFlavorException ex) {
						ex.printStackTrace();
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
			e.dropComplete(true);
			super.drop(e);
		}
	}
}
