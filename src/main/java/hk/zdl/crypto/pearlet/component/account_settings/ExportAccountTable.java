package hk.zdl.crypto.pearlet.component.account_settings;

import java.awt.Component;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

import com.csvreader.CsvWriter;

import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class ExportAccountTable {
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();

	public static final void export_csv(Component c, TableModel m) {
		var w = SwingUtilities.getWindowAncestor(c);
		var file_dialog = new JFileChooser();
		file_dialog.setMultiSelectionEnabled(false);
		file_dialog.setDragEnabled(false);
		file_dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		file_dialog.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return rsc_bdl.getString("GENERAL.CSV_FILES");
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
			}
		});
		int o = file_dialog.showSaveDialog(w);
		if (o != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = file_dialog.getSelectedFile();
		if (!file.getName().endsWith(".csv")) {
			file = new File(file.getAbsolutePath() + ".csv");
		}
		if (file.isFile() && file.exists() && !file.canWrite()) {
			JOptionPane.showMessageDialog(w, rsc_bdl.getString("SETTINGS.ACCOUNT.EXPORT.MSG.UNWRITABLE"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			var out = new FileOutputStream(file);
			var writer = new CsvWriter(out, ',', Charset.defaultCharset());
			var col_name = new String[m.getColumnCount()];
			for (int i = 0; i < col_name.length; i++) {
				col_name[i] = m.getColumnName(i);
			}
			writer.writeRecord(col_name);
			for (int i = 0; i < m.getRowCount(); i++) {
				var line = new String[m.getColumnCount()];
				for (int j = 0; j < line.length; j++) {
					var val = m.getValueAt(i, j);
					line[j] = val == null ? "" : val.toString();
				}
				writer.writeRecord(line);
			}
			writer.flush();
			writer.close();
			out.flush();
			out.close();
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(w, t.getMessage(), t.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			return;
		}
		UIUtil.displayMessage("", rsc_bdl.getString("SETTINGS.ACCOUNT.EXPORT.MSG.DONE"), MessageType.INFO);
	}
}
