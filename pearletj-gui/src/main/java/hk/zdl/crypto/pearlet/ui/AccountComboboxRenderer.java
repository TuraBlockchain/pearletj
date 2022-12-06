package hk.zdl.crypto.pearlet.ui;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import hk.zdl.crypto.pearlet.ds.AccountComboboxEntry;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.SignumAddress;

public class AccountComboboxRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 266879026689185888L;

	private static boolean show_numberic = Boolean.parseBoolean(Util.getUserSettings().getProperty("show_numberic_id"));

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		var entry = (AccountComboboxEntry) value;
		var str = "";
		if (entry.nickname != null) {
			str = entry.nickname;
		} else if (show_numberic && Arrays.asList(CrptoNetworks.ROTURA, CrptoNetworks.SIGNUM).contains(entry.network)) {
			str = SignumAddress.fromEither(entry.address).getID();
		} else {
			str = entry.address;
		}
		return super.getListCellRendererComponent(list, str, index, isSelected, cellHasFocus);
	}

}
