package hk.zdl.crypto.pearlet.ui;

import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import hk.zdl.crypto.pearlet.util.CryptoUtil;

public class TxAmountCellRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 338195977949445525L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value != null) {
			BigDecimal bd = (BigDecimal) value;
			bd = bd.setScale(CryptoUtil.peth_decimals);
			value = bd;
		}
		setFont(new Font(Font.MONOSPACED, Font.PLAIN, table.getFont().getSize()));
		setHorizontalAlignment(SwingConstants.RIGHT);
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		return this;
	}

}
