package hk.zdl.crypto.pearlet.component.account_settings.signum;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Files;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.greenrobot.eventbus.EventBus;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import com.csvreader.CsvReader;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class ImportSignumAccount {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);

	@SuppressWarnings("unchecked")
	public static final void create_import_account_dialog(Component c, CryptoNetwork nw) {
		var w = SwingUtilities.getWindowAncestor(c);
		Icon icon = UIUtil.getStretchIcon("icon/" + "wallet_2.svg", 64, 64);
		var panel = new JPanel(new GridBagLayout());
		var label_1 = new JLabel("Network:");
		var network_combobox = new JComboBox<>(new String[] { nw.toString() });
		network_combobox.setEnabled(false);
		var label_2 = new JLabel("Text type:");
		var combobox_1 = new JComboBox<>(new EnumComboBoxModel<>(PKT.class));
		panel.add(label_1, new GridBagConstraints(0, 0, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));
		panel.add(network_combobox, new GridBagConstraints(1, 0, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));
		panel.add(label_2, new GridBagConstraints(2, 0, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));
		panel.add(combobox_1, new GridBagConstraints(3, 0, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));
		var text_area = new JTextArea(5, 30);
		var scr_pane = new JScrollPane(text_area);
		panel.add(scr_pane, new GridBagConstraints(0, 1, 4, 3, 0, 0, 17, 1, new Insets(5, 5, 0, 5), 0, 0));

		int i = JOptionPane.showConfirmDialog(w, panel, "Import Existing Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
		if (i == JOptionPane.OK_OPTION) {
			PKT type = (PKT) combobox_1.getSelectedItem();
			String text = text_area.getText().trim();

			boolean b = false;
			byte[] public_key, private_key;
			try {
				private_key = CryptoUtil.getPrivateKey(nw, type, text);
				public_key = CryptoUtil.getPublicKey(nw, private_key);
				b = MyDb.insertAccount(nw, CryptoUtil.getAddress(nw, public_key), public_key, private_key);
			} catch (Exception x) {
				JOptionPane.showMessageDialog(w, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (b) {
				UIUtil.displayMessage("Import Account", "Done!");
				Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
			} else {
				JOptionPane.showMessageDialog(w, "Duplicate Entry!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static final void batch_import(Component c, CryptoNetwork nw) {
		var w = SwingUtilities.getWindowAncestor(c);
		var file_dialog = new JFileChooser();
		file_dialog.setDialogType(JFileChooser.OPEN_DIALOG);
		file_dialog.setMultiSelectionEnabled(false);
		file_dialog.setDragEnabled(false);
		file_dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		file_dialog.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "CSV Files";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
			}
		});
		int i = file_dialog.showOpenDialog(w);
		if (i != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = file_dialog.getSelectedFile();
		if (file == null) {
			return;
		}

		try {
			var reader = new CsvReader(Files.newBufferedReader(file.toPath()), ',');
			reader.readHeaders();
			int header_index = reader.getIndex("phrase"), total = 0, imported = 0;
			while (reader.readRecord()) {
				var phrase = reader.get(header_index);
				total++;
				try {
					byte[] private_key = CryptoUtil.getPrivateKey(nw, PKT.Phrase, phrase);
					byte[] public_key = CryptoUtil.getPublicKey(nw, private_key);
					boolean b = MyDb.insertAccount(nw, CryptoUtil.getAddress(nw, public_key), public_key, private_key);
					if(b) {
						imported++;
					}
				} catch (Exception x) {
					continue;
				}
			}
			reader.close();
			Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
			var panel = new JPanel(new GridBagLayout());
			var label_1 = new JLabel("Imported:");
			panel.add(label_1, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
			var label_2 = new JLabel(""+imported);
			label_2.setHorizontalAlignment(SwingConstants.RIGHT);
			label_2.setHorizontalTextPosition(SwingConstants.RIGHT);
			panel.add(label_2, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
			var label_3 = new JLabel("Total:");
			panel.add(label_3, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
			var label_4 = new JLabel(""+total);
			label_4.setHorizontalAlignment(SwingConstants.RIGHT);
			label_4.setHorizontalTextPosition(SwingConstants.RIGHT);
			panel.add(label_4, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
			JOptionPane.showMessageDialog(w, panel, "Done", JOptionPane.INFORMATION_MESSAGE);
		} catch (Throwable x) {
			while (x.getCause() != null) {
				x = x.getCause();
			}
			JOptionPane.showMessageDialog(w, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

}
