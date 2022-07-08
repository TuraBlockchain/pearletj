package hk.zdl.crypto.pearlet.component.account_settings.web3j;

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import hk.zdl.crypto.pearlet.ui.UIUtil;

public class ImportWeb3JAccountFromFile {

	public static final void create_import_account_dialog(Component c) {
		var w = SwingUtilities.getWindowAncestor(c);
		Icon icon = UIUtil.getStretchIcon("icon/" + "wallet_2.svg", 64, 64);
		var file_dialog = new JFileChooser();
		file_dialog.setDialogType(JFileChooser.OPEN_DIALOG);
		file_dialog.setMultiSelectionEnabled(false);
		file_dialog.setDragEnabled(false);
		file_dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		file_dialog.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "JSON Files";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().endsWith(".json");
			}
		});
		int i = file_dialog.showOpenDialog(w);
		if (i != JFileChooser.APPROVE_OPTION) {
			return;
		}
	}
}
