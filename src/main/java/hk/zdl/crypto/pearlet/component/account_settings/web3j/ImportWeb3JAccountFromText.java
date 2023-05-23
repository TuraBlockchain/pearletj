package hk.zdl.crypto.pearlet.component.account_settings.web3j;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.greenrobot.eventbus.EventBus;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class ImportWeb3JAccountFromText {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);

	public static final void import_from_private_key(Component c, CryptoNetwork nw) {
		var w = SwingUtilities.getWindowAncestor(c);
		Icon icon = UIUtil.getStretchIcon("icon/" + "wallet_2.svg", 64, 64);
		var panel = new JPanel(new GridBagLayout());

		var mm_label = new JLabel("Enter your private key in Hex:");
		var tx_field = new JTextArea(5, 20);
		var sc_panee = new JScrollPane(tx_field);
		panel.add(mm_label, new GridBagConstraints(0, 0, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));
		panel.add(sc_panee, new GridBagConstraints(0, 1, 2, 1, 0, 0, 17, 1, insets_5, 0, 0));

		int i = JOptionPane.showConfirmDialog(w, panel, "Import Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
		if (i != JOptionPane.OK_OPTION) {
			return;
		}
		String private_key_str = tx_field.getText().trim();

		if (private_key_str.isBlank()) {
			JOptionPane.showMessageDialog(w, "Private key cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Credentials cred = null;

		try {
			cred = Credentials.create(private_key_str);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(w, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		ECKeyPair eckp = cred.getEcKeyPair();
		boolean b = MyDb.insertAccount(nw, cred.getAddress(), Numeric.toBytesPadded(eckp.getPublicKey(), 64), Numeric.toBytesPadded(eckp.getPrivateKey(), 32));
		if (b) {
			UIUtil.displayMessage("Import Account", "Done!");
			Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
		} else {
			JOptionPane.showMessageDialog(w, "Duplicate Entry!", "Error", JOptionPane.ERROR_MESSAGE);
		}

	}

	public static final void load_from_mnemonic(Component c, CryptoNetwork nw) {
		var w = SwingUtilities.getWindowAncestor(c);
		Icon icon = UIUtil.getStretchIcon("icon/" + "wallet_2.svg", 64, 64);
		var panel = new JPanel(new GridBagLayout());

		var mm_label = new JLabel("Enter your mnemonic:");
		var tx_field = new JTextArea(5, 20);
		var sc_panee = new JScrollPane(tx_field);
		panel.add(mm_label, new GridBagConstraints(0, 0, 1, 1, 0, 0, 17, 1, insets_5, 0, 0));
		panel.add(sc_panee, new GridBagConstraints(0, 1, 2, 1, 0, 0, 17, 1, insets_5, 0, 0));

		int i = JOptionPane.showConfirmDialog(w, panel, "Import Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
		if (i != JOptionPane.OK_OPTION) {
			return;
		}
		String mnemonic = tx_field.getText().trim();

		if (mnemonic.isBlank()) {
			JOptionPane.showMessageDialog(w, "Mnemonic cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Credentials cred = null;

		try {
			cred = WalletUtils.loadBip39Credentials("", mnemonic);
		} catch (Exception e) {
		}

		if (cred == null) {
			try {
				cred = WalletUtils.loadBip39Credentials(mnemonic, mnemonic);
			} catch (Exception e) {
			}
		}
		if (cred == null) {
			var pw_field = new JPasswordField(20);
			int j = JOptionPane.showConfirmDialog(w, pw_field, "Enter password for wallet", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
			if (j != JOptionPane.OK_OPTION) {
				return;
			}
			try {
				cred = WalletUtils.loadBip39Credentials(new String(pw_field.getPassword()), mnemonic);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(w, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

		}
		ECKeyPair eckp = cred.getEcKeyPair();
		boolean b = MyDb.insertAccount(nw, cred.getAddress(), Numeric.toBytesPadded(eckp.getPublicKey(), 64), Numeric.toBytesPadded(eckp.getPrivateKey(), 32));
		if (b) {
			UIUtil.displayMessage("Import Account", "Done!");
			Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
		} else {
			JOptionPane.showMessageDialog(w, "Duplicate Entry!", "Error", JOptionPane.ERROR_MESSAGE);
		}

	}
}
