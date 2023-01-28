package hk.zdl.crypto.pearlet.component.miner.remote.mining.renderer;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import hk.zdl.crypto.pearlet.ds.RoturaAddress;
import hk.zdl.crypto.pearlet.util.Util;

public class IDRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5343725138634437356L;
	private static boolean show_numberic = Boolean.parseBoolean(Util.getUserSettings().getProperty("show_numberic_id"));

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		var adr = RoturaAddress.fromEither(value.toString());
		if (show_numberic) {
			value = adr.getID();
		} else {
			value = adr.getFullAddress();
		}
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
		return this;
	}

}
