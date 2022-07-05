package hk.zdl.crpto.pearlet.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class UIUtil {

	public static final void adjust_table_width(JTable table, TableColumnModel table_column_model) {
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 100; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}
			if (width > 300)
				width = 300;
			table_column_model.getColumn(column).setMinWidth(width);
			table_column_model.getColumn(column).setPreferredWidth(width);
		}
	}
}
