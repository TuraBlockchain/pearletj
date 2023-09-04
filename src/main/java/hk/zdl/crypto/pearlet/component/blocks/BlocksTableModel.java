package hk.zdl.crypto.pearlet.component.blocks;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class BlocksTableModel extends AbstractTableModel {

	private static final String[] columnNames = new String[] { "Block id", "Height", "Date", "Reward", "No. of Tx." };
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

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data.get(rowIndex)[columnIndex] = aValue;
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	public final void setData(List<Object[]> data) {
		clearData();
		this.data.addAll(data);
		fireTableRowsInserted(0, data.size());
	}

	public final void clearData() {
		fireTableRowsDeleted(0, this.data.size());
		this.data.clear();
	}

	public final void insertData(Object[] entry) {
		data.add(entry);
		fireTableRowsInserted(data.size() - 1, data.size());
	}

}
