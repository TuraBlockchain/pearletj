package hk.zdl.crypto.pearlet.component.miner.remote.mining;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.json.JSONObject;

public class MinerErrorCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -1777092869579013934L;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setToolTipText(null);
		if (value != null && value instanceof JSONObject) {
			var jobj = (JSONObject) value;
			var msg = jobj.optString("msg");
			var time = jobj.optLong("time");
			super.getTableCellRendererComponent(table, msg, isSelected, hasFocus, row, column);
			if (time != 0) {
				setToolTipText(sdf.format(new Date(time)));
			}
			return this;
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

}
