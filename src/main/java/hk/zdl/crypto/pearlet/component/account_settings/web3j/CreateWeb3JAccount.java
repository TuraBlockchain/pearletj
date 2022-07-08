package hk.zdl.crypto.pearlet.component.account_settings.web3j;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.greenrobot.eventbus.EventBus;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class CreateWeb3JAccount {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);

	public static final void create_new_account_dialog(Component c) {
		var w = SwingUtilities.getWindowAncestor(c);
		Icon icon = UIUtil.getStretchIcon("icon/" + "cloud-plus-fill.svg", 64, 64);
		var panel = new JPanel(new GridBagLayout());

		var pw_label = new JLabel("Enter password for wallet:");
		var pw_field = new JPasswordField(20);
		panel.add(pw_label, new GridBagConstraints(0, 0, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));
		panel.add(pw_field, new GridBagConstraints(1, 0, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));

		var mm_label = new JLabel("Enter your mnemonic:");
		var tx_field = new JTextArea(5, 20);
		var sc_panee = new JScrollPane(tx_field,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(mm_label, new GridBagConstraints(0, 1, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));
		panel.add(sc_panee, new GridBagConstraints(0, 2, 2, 1, 0, 0, 17, 1, insets_5, 0, 0));

		int i = JOptionPane.showConfirmDialog(w, panel, "Create New Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
		if (i == JOptionPane.CANCEL_OPTION) {
			return;
		} else if (i == JOptionPane.OK_OPTION) {
			if (tx_field.getText().isBlank()) {
				JOptionPane.showMessageDialog(w, "Mnemonic is required to generate a seed", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			var file_dialog = new JFileChooser();
			file_dialog.setDialogType(JFileChooser.SAVE_DIALOG);
			file_dialog.setDialogTitle("Save Wallet File to...");
			file_dialog.setMultiSelectionEnabled(false);
			file_dialog.setDragEnabled(false);
			file_dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			i = file_dialog.showSaveDialog(w);
			if (i == JFileChooser.APPROVE_OPTION) {
				try {
					WalletUtils.generateBip39WalletFromMnemonic(new String(pw_field.getPassword()), tx_field.getText().trim(), file_dialog.getSelectedFile());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(w, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				ECKeyPair eckp = WalletUtils.loadBip39Credentials(new String(pw_field.getPassword()), tx_field.getText().trim()).getEcKeyPair();
				boolean b = MyDb.insertAccount(eckp);
				if(b) {
					UIUtil.displayMessage("Create Account", "done!", null);
					Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
				}else {
					JOptionPane.showMessageDialog(w, "Something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
}
