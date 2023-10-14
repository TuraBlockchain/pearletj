package hk.zdl.crypto.pearlet.component.account_settings.burst;

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
import signumj.entity.SignumAddress;

public class WatchBurstAccount {

	public static final void create_watch_account_dialog(Component c, CryptoNetwork nw) {
		var w = SwingUtilities.getWindowAncestor(c);
		Icon icon = UIUtil.getStretchIcon("icon/" + "eyeglasses.svg", 64, 64);
		String address = String.valueOf(JOptionPane.showInputDialog(w, "Please input account address or numeric id:", "Watch Account", JOptionPane.PLAIN_MESSAGE, icon, null, null)).trim();
		if ("null".equals(String.valueOf(address)) || address.isBlank()) {
			return;
		}
		byte[] public_key = null, private_key = new byte[0];
		boolean b = false;
		try {
			if (nw.isBurst()) {
				SignumAddress adr = SignumAddress.fromEither(address);
				if (adr == null) {
					JOptionPane.showMessageDialog(w, "Invalid address!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					address = adr.getRawAddress();
					public_key = adr.getPublicKey();
					address = CryptoUtil.getConstants(nw).getString("addressPrefix") + "-" + address;
				}

			}
			if (public_key == null) {
				public_key = new byte[0];
				try {
					public_key = CryptoUtil.getAccount(nw, address).getPublicKey();
				} catch (Exception e) {
				}
			}
			b = MyDb.insert_or_update_account(nw, address, public_key, private_key);
		} catch (Exception x) {
			JOptionPane.showMessageDialog(w, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (b) {
			UIUtil.displayMessage("Watch Account", "Done!");
			EventBus.getDefault().post(new AccountListUpdateEvent());
		} else {
			JOptionPane.showMessageDialog(w, "Duplicate Entry!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

}
