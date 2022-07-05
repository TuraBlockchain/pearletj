package hk.zdl.crpto.pearlet.component.dashboard.signum;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import signumj.entity.SignumValue;
import signumj.entity.response.Transaction;

@SuppressWarnings("serial")
public class SignumValueCellRenderer extends DefaultTableCellRenderer {

	private final String address;

	public SignumValueCellRenderer(String address) {
		this.address = address;
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setBackground(new Color(0, 175, 175));
		if (!isSelected) {
			Transaction tx = (Transaction) value;
			if (tx.getType() == 0) {// Payment
				if (tx.getRecipient().getFullAddress().equals(address)) {
					setBackground(new Color(175, 255, 175));
				}else{
					setBackground(Color.pink);
				}
			}
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

	@Override
	protected void setValue(Object value) {
		Transaction tx = (Transaction) value;
		SignumValue val = tx.getAmount();
		super.setValue(Character.valueOf((char) 0xA7A8)+val.toSigna().toPlainString());
	}

}
