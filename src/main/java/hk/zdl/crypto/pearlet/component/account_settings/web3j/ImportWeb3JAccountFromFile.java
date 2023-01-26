package hk.zdl.crypto.pearlet.component.account_settings.web3j;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.greenrobot.eventbus.EventBus;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;

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
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".json");
			}
		});
		int i = file_dialog.showOpenDialog(w);
		if (i != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = file_dialog.getSelectedFile();
		Credentials cred = null;
		try {
			cred = WalletUtils.loadCredentials("", file);
		} catch (IOException | CipherException e) {
		}
		var pw_field = new JPasswordField(20);
		int j = JOptionPane.showConfirmDialog(w, pw_field, "Enter password for wallet", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
		if (j != JOptionPane.OK_OPTION) {
			return;
		}
		try {
			cred = WalletUtils.loadCredentials(new String(pw_field.getPassword()), file);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(w, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		ECKeyPair eckp = cred.getEcKeyPair();
		boolean b = MyDb.insertAccount(CrptoNetworks.WEB3J, cred.getAddress(),Numeric.toBytesPadded(eckp.getPublicKey(), 64), Numeric.toBytesPadded(eckp.getPrivateKey(), 32));
		if (b) {
			UIUtil.displayMessage("Import Account", "Done!", null);
			Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
		} else {
			JOptionPane.showMessageDialog(w, "Duplicate Entry!", "Error", JOptionPane.ERROR_MESSAGE);
		}

		
	}
}
