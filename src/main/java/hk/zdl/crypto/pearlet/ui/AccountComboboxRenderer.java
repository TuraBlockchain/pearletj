package hk.zdl.crypto.pearlet.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import hk.zdl.crypto.pearlet.ds.AccountComboboxEntry;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.SignumAddress;

public class AccountComboboxRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 266879026689185888L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		var entry = (AccountComboboxEntry) value;
		var str = "";
		if (entry != null) {
			var show_numberic = Util.getUserSettings().getBoolean("show_numberic_id", false);
			if (entry.nickname != null) {
				str = entry.nickname;
			} else if (show_numberic && entry.network.isBurst()) {
				str = SignumAddress.fromEither(entry.address).getID();
			} else {
				str = entry.address;
			}
		}
		return super.getListCellRendererComponent(list, str, index, isSelected, cellHasFocus);
	}

}
