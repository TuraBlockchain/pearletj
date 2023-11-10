package hk.zdl.crypto.pearlet.component.account_settings.burst;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.greenrobot.eventbus.EventBus;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.SignumAddress;

public class WatchBurstAccount {
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();

	public static final void create_watch_account_dialog(Component c, CryptoNetwork nw) {
		var w = SwingUtilities.getWindowAncestor(c);
		Icon icon = UIUtil.getStretchIcon("icon/" + "eyeglasses.svg", 64, 64);
		var txt_field = new JTextField(30);
		txt_field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, txt_field.getFont().getSize()));
		var pane = new JOptionPane(txt_field, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, icon);
		var dlg = pane.createDialog(w, rsc_bdl.getString("SETTINGS.ACCOUNT.WATCH.INPUT_TEXT"));
		dlg.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				txt_field.grabFocus();
			}
		});
		dlg.setVisible(true);
		if ((int) pane.getValue() != JOptionPane.OK_OPTION) {
			return;
		}
		var address = txt_field.getText();
		if(address.isBlank()) {
			JOptionPane.showMessageDialog(w, rsc_bdl.getString("ALISE.SEARCH.PANEL.ADDR_CANNOT_BE_EMPTY"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		byte[] public_key = null, private_key = new byte[0];
		boolean b = false;
		try {
			if (nw.isBurst()) {
				var adr = SignumAddress.fromEither(address);
				if (adr == null) {
					JOptionPane.showMessageDialog(w, rsc_bdl.getString("GENERAL.ADDRESS_INVALID"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					address = adr.getRawAddress();
					public_key = adr.getPublicKey();
					address = CryptoUtil.getConstants(nw).getString("addressPrefix") + "-" + address;
				}

			}
			if (public_key == null) {
				try {
					public_key = CryptoUtil.getAccount(nw, address).getPublicKey();
				} catch (Exception e) {
					public_key = new byte[0];
				}
			}
			b = MyDb.insert_or_update_account(nw, address, public_key, private_key);
		} catch (Exception x) {
			JOptionPane.showMessageDialog(w, x.getMessage(), x.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (b) {
			UIUtil.displayMessage(rsc_bdl.getString("SETTINGS.ACCOUNT.WATCH.TITLE"), rsc_bdl.getString("GENERAL.DONE"));
			EventBus.getDefault().post(new AccountListUpdateEvent());
		} else {
			JOptionPane.showMessageDialog(w, rsc_bdl.getString("GENERAL.DUP"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
		}
	}

}
