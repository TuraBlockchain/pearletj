package hk.zdl.crypto.pearlet.component.account_settings.web3j;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.greenrobot.eventbus.EventBus;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class WatchWeb3JAccount {

	public static final void create_watch_account_dialog(Component c, CryptoNetwork nw) {
		var w = SwingUtilities.getWindowAncestor(c);
		Icon icon = UIUtil.getStretchIcon("icon/" + "eyeglasses.svg", 64, 64);
		String address = String.valueOf(JOptionPane.showInputDialog(w, "Please input Ethernum address:", "Watch Account", JOptionPane.PLAIN_MESSAGE, icon, null, null));
		if ("null".equals(String.valueOf(address)) || address.isBlank()) {
			return;
		}
		address = address.trim();
		if (CryptoUtil.isValidAddress(nw, address)) {
			if (MyDb.insertAccount(nw, address, new byte[] {}, new byte[] {})) {
				UIUtil.displayMessage("Watch Account", "done!");
				Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
			} else {
				JOptionPane.showMessageDialog(w, "Duplicate Entry!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(w, "Invalid address", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
}
