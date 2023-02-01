package hk.zdl.crypto.pearlet.ui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class TxAmountCellRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 338195977949445525L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setFont(new Font(Font.MONOSPACED, Font.PLAIN, table.getFont().getSize()));
		setHorizontalAlignment(SwingConstants.RIGHT);
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		return this;
	}

}
