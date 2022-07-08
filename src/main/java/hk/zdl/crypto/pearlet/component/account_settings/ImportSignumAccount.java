package hk.zdl.crypto.pearlet.component.account_settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.greenrobot.eventbus.EventBus;

import hk.zdl.crypto.pearlet.component.MyStretchIcon;
import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.misc.IndepandentWindows;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class ImportSignumAccount {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	public static final void create_import_account_dialog(Component c) {
		var w = SwingUtilities.getWindowAncestor(c);
		var dialog = new JDialog(w, "Import Existing Account", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		IndepandentWindows.add(dialog);
		var panel = new JPanel(new GridBagLayout());
		try {
			panel.add(new JLabel(new MyStretchIcon(ImageIO.read(Util.getResource("icon/" + "wallet_2.svg")), 64, 64)), new GridBagConstraints(0, 0, 1, 4, 0, 0, 17, 0, insets_5, 0, 0));
		} catch (IOException e) {
		}
		var label_1 = new JLabel("Network:");
		var network_combobox = new JComboBox<>(new String[] { "ROTURA", "SIGNUM" });
		var label_2 = new JLabel("Text type:");
		var combobox_1 = new JComboBox<>(new String[] { "Phrase", "HEX", "Base64" });
		panel.add(label_1, new GridBagConstraints(1, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(network_combobox, new GridBagConstraints(2, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(label_2, new GridBagConstraints(3, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(combobox_1, new GridBagConstraints(4, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		var text_area = new JTextArea(5, 30);
		var scr_pane = new JScrollPane(text_area);
		panel.add(scr_pane, new GridBagConstraints(1, 1, 4, 3, 0, 0, 17, 0, new Insets(5, 5, 0, 5), 0, 0));
		var btn_1 = new JButton("OK");
		btn_1.addActionListener(e -> Util.submit(() -> {
			CrptoNetworks nw = CrptoNetworks.valueOf(network_combobox.getSelectedItem().toString());
			String type = combobox_1.getSelectedItem().toString();
			String text = text_area.getText().trim();

			boolean b = false;
			byte[] public_key, private_key;
			try {
				private_key = CryptoUtil.getPrivateKey(nw, type, text);
				public_key = CryptoUtil.getPublicKey(nw, private_key);
				b = MyDb.insertAccount(nw, public_key, private_key);
			} catch (Exception x) {
				JOptionPane.showMessageDialog(dialog, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (b) {
				dialog.dispose();
				Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
			} else {
				JOptionPane.showMessageDialog(dialog, "Something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}));
		var panel_1 = new JPanel(new BorderLayout());
		panel_1.add(panel, BorderLayout.CENTER);
		var panel_2 = new JPanel(new FlowLayout());
		panel_2.add(btn_1);
		panel_1.add(panel_2, BorderLayout.SOUTH);

		dialog.add(panel_1);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(w);
		dialog.setVisible(true);
	}

}
