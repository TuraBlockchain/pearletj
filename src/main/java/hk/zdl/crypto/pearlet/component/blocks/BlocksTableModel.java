package hk.zdl.crypto.pearlet.component.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javax.swing.table.AbstractTableModel;

import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class BlocksTableModel extends AbstractTableModel {

	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	private static final List<String> columnNames = Stream.of("ID","HEIGHT","DATE","REWARD","TX_COUNT").map(s->rsc_bdl.getString("TABLE.COLUNM_NAME.BLOCK."+s)).toList();
	private final List<Object[]> data = new ArrayList<>();

	@Override
	public int getColumnCount() {
		return columnNames.size();
	}

	@Override
	public String getColumnName(int column) {
		return columnNames.get(column);
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
