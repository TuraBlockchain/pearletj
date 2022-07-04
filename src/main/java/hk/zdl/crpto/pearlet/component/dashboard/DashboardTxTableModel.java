package hk.zdl.crpto.pearlet.component.dashboard;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class DashboardTxTableModel extends AbstractTableModel {

	private static final String[] columnNames = new String[] { "Tx id", "Date", "Type", "Amount", "Account" };
	private final List<Object[]> data = new ArrayList<>();

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex)[columnIndex];
	}

	public final void setData(List<Object[]> data) {
		fireTableRowsDeleted(0, this.data.size());
		this.data.clear();
		this.data.addAll(data);
		fireTableRowsInserted(0, data.size());
	}
}
