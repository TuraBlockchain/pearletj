package hk.zdl.crypto.pearlet.component.account_settings.signum;

import static hk.zdl.crypto.pearlet.util.CrptoNetworks.ROTURA;
import static hk.zdl.crypto.pearlet.util.CrptoNetworks.SIGNUM;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.greenrobot.eventbus.EventBus;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.ds.RoturaAddress;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.SignumAddress;

public class WatchSignumAccount {

	public static final void create_watch_account_dialog(Component c, CrptoNetworks nw) {
		var w = SwingUtilities.getWindowAncestor(c);
		Icon icon = UIUtil.getStretchIcon("icon/" + "eyeglasses.svg", 64, 64);
		String address = String.valueOf(JOptionPane.showInputDialog(w, "Please input account address or numberic id:", "Watch Account", JOptionPane.PLAIN_MESSAGE, icon, null, null));
		if ("null".equals(String.valueOf(address)) || address.isBlank()) {
			return;
		}
		address = address.trim();
		byte[] public_key = null, private_key = new byte[0];
		if (SIGNUM.equals(nw)) {
			SignumAddress adr = SignumAddress.fromEither(address);
			if (adr == null) {
				JOptionPane.showMessageDialog(w, "Invalid address!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				public_key = adr.getPublicKey();
			}
		} else if (ROTURA.equals(nw)) {
			RoturaAddress adr = RoturaAddress.fromEither(address);
			if (adr == null) {
				JOptionPane.showMessageDialog(w, "Invalid address!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				public_key = adr.getPublicKey();
			}
		}
		if (public_key == null) {
			public_key = new byte[0];
		}
		boolean b = false;
		try {
			b = MyDb.insertAccount(nw, address, public_key, private_key);
		} catch (Exception x) {
			JOptionPane.showMessageDialog(w, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (b) {
			UIUtil.displayMessage("Watch Account", "done!", null);
			Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
		} else {
			JOptionPane.showMessageDialog(w, "Duplicate Entry!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

}
