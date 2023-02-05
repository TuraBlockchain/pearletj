package hk.zdl.crypto.pearlet.component.miner.remote.mining;

import javax.swing.table.AbstractTableModel;

import org.json.JSONArray;

public class MinerStateTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 5805340052864846718L;
	public static final String[] column_name = new String[] { "ID", "Start Time", "File Count", "Capacity", "Plot File Directory", "Last Refresh", "Round Time", "Speed", "Height", "Scoop",
			"Status" };
	private final JSONArray jarr = new JSONArray();

	@Override
	public int getColumnCount() {
		return column_name.length;
	}

	@Override
	public int getRowCount() {
		return jarr.length();
	}

	@Override
	public String getColumnName(int column) {
		return column_name[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		var jobj = jarr.optJSONObject(rowIndex);
		if (jobj == null) {
			return null;
		}
		switch (columnIndex) {
		case 0:
			return jobj.opt("id");
		case 1:
			return jobj.opt("start time");
		case 2:
			return jobj.opt("file count");
		case 3:
			return jobj.opt("capacity");
		case 4:
			return jobj.opt("plot_dirs");
		case 5:
			return jobj.opt("last refresh");
		case 6:
			return jobj.opt("roundtime");
		case 7:
			return jobj.optString("speed", "").replace("inf", "∞");
		case 8:
			return jobj.opt("height");
		case 9:
			return jobj.opt("scoop");
		case 10:
			return jobj.opt("status");
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public final void setData(JSONArray jarr) {
		var old_size = this.jarr.length();
		var new_size = jarr.length();
		this.jarr.clear();
		for (var i = 0; i < jarr.length(); i++) {
			this.jarr.put(jarr.get(i));
		}
		if (new_size > old_size) {
			fireTableRowsInserted(old_size, new_size - old_size);
		} else if (new_size < old_size) {
			fireTableRowsDeleted(new_size, old_size);
		}
		fireTableRowsUpdated(0, Math.min(old_size, new_size));
	}

	public final void clearData() {
		this.jarr.clear();
		fireTableRowsDeleted(0, jarr.length());
	}

}
