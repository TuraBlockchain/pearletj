package hk.zdl.crypto.pearlet.component.miner.remote.mining.renderer;

import java.awt.Component;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.json.JSONObject;

import hk.zdl.crypto.pearlet.util.Util;

public class MinerStatusCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -1777092869579013934L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setToolTipText(null);
		if (value != null && value instanceof JSONObject) {
			var jobj = (JSONObject) value;
			var msg = jobj.optString("msg");
			var time = jobj.optLong("time");
			super.getTableCellRendererComponent(table, msg, isSelected, hasFocus, row, column);
			if (time != 0) {
				setToolTipText(Util.getDateFormat().format(new Date(time)));
			}
			return this;
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

}
