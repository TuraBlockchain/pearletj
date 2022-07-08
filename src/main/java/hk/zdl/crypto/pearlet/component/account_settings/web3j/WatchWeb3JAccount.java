package hk.zdl.crypto.pearlet.component.account_settings.web3j;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;

public class WatchWeb3JAccount {

	public static final void create_watch_account_dialog(Component c) {
		var w = SwingUtilities.getWindowAncestor(c);
		Icon icon = UIUtil.getStretchIcon("icon/" + "eyeglasses.svg", 64, 64);
		String address = String.valueOf(JOptionPane.showInputDialog(w, null, "Watch Account", JOptionPane.PLAIN_MESSAGE, icon, null, null));
		if (address == null || address.isBlank()||address.equals("null")) {
			return;
		}
		address = address.trim();
		if(WalletUtils.isValidAddress(address)) {
			byte[] public_key = Numeric.toBytesPadded(Numeric.toBigInt(address), 64);
			MyDb.insertAccount(CrptoNetworks.WEB3J, address, public_key, new byte[] {});
		}else {
			JOptionPane.showMessageDialog(w, "Invalid address", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
}
