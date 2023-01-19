package hk.zdl.crypto.pearlet.component.miner.remote.mining;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.json.JSONArray;

public class PlotDirCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 6149163500390266798L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setToolTipText(null);
		if (value != null && value instanceof JSONArray) {
			var jarr = (JSONArray) value;
			if (jarr.length() == 0) {
				value = null;
			} else if (jarr.length() == 1) {
				value = jarr.get(0);
			}else {
				value = "<MULTI>";
				var sb = new StringBuilder();
				for(var i=0;i<jarr.length();i++) {
					sb.append(jarr.getString(i));
					sb.append('\n');
				}
				setToolTipText(sb.toString());
			}
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

	}

}
