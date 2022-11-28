package hk.zdl.crypto.pearlet.ui;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ProgressBarTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 7965789081442537931L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value instanceof Float) {
			var bar = new JProgressBar(0, 100);
			bar.setValue((int) Float.parseFloat(value.toString()));
			bar.setString(bar.getValue() + "%");
			bar.setStringPainted(true);
			bar.setFont(table.getFont());
			bar.setBorder(super.getBorder());
			bar.setMinimumSize(super.getMinimumSize());
			bar.setMaximumSize(super.getMaximumSize());
			bar.setPreferredSize(super.getPreferredSize());
			return bar;
		} else {
			return this;
		}
	}
}
