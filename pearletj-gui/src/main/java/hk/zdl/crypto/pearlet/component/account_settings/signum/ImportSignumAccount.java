package hk.zdl.crypto.pearlet.component.account_settings.signum;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.greenrobot.eventbus.EventBus;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class ImportSignumAccount {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);

	public static final void create_import_account_dialog(Component c, CrptoNetworks nw) {
		var w = SwingUtilities.getWindowAncestor(c);
		Icon icon = UIUtil.getStretchIcon("icon/" + "wallet_2.svg", 64, 64);
		var panel = new JPanel(new GridBagLayout());
		var label_1 = new JLabel("Network:");
		var network_combobox = new JComboBox<>(new String[] { nw.toString() });
		network_combobox.setEnabled(false);
		var label_2 = new JLabel("Text type:");
		var combobox_1 = new JComboBox<>(new String[] { "Phrase", "HEX", "Base64" });
		panel.add(label_1, new GridBagConstraints(0, 0, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));
		panel.add(network_combobox, new GridBagConstraints(1, 0, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));
		panel.add(label_2, new GridBagConstraints(2, 0, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));
		panel.add(combobox_1, new GridBagConstraints(3, 0, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));
		var text_area = new JTextArea(5, 30);
		var scr_pane = new JScrollPane(text_area);
		panel.add(scr_pane, new GridBagConstraints(0, 1, 4, 3, 0, 0, 17, 1, new Insets(5, 5, 0, 5), 0, 0));

		int i = JOptionPane.showConfirmDialog(w, panel, "Import Existing Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
		if (i == JOptionPane.OK_OPTION) {
			String type = combobox_1.getSelectedItem().toString();
			String text = text_area.getText().trim();

			boolean b = false;
			byte[] public_key, private_key;
			try {
				private_key = CryptoUtil.getPrivateKey(nw, type, text);
				public_key = CryptoUtil.getPublicKey(nw, private_key);
				b = MyDb.insertAccount(nw, CryptoUtil.getAddress(nw, public_key),public_key, private_key);
			} catch (Exception x) {
				JOptionPane.showMessageDialog(w, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (b) {
				Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
			} else {
				JOptionPane.showMessageDialog(w, "Duplicate Entry!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
