package hk.zdl.crypto.pearlet.component.account_settings.web3j;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

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

public class WatchWeb3JAccount {
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();

	public static final void create_watch_account_dialog(Component c, CryptoNetwork nw) {
		var w = SwingUtilities.getWindowAncestor(c);
		var icon = UIUtil.getStretchIcon("icon/" + "eyeglasses.svg", 64, 64);
		var txt_field = new JTextField(30);
		txt_field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, txt_field.getFont().getSize()));
		var pane = new JOptionPane(txt_field, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, icon);
		var dlg = pane.createDialog(w, rsc_bdl.getString("SETTINGS.ACCOUNT.WATCH.ETH.INPUT_TEXT"));
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
		var address = txt_field.getText().trim();
		if (CryptoUtil.isValidAddress(nw, address)) {
			if (MyDb.insert_or_update_account(nw, address, new byte[] {}, new byte[] {})) {
				UIUtil.displayMessage(rsc_bdl.getString("SETTINGS.ACCOUNT.WATCH.TITLE"), rsc_bdl.getString("GENERAL.DONE"));
				EventBus.getDefault().post(new AccountListUpdateEvent());
			} else {
				JOptionPane.showMessageDialog(w, rsc_bdl.getString("GENERAL.DUP"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(w, rsc_bdl.getString("GENERAL.ADDRESS_INVALID"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
}
