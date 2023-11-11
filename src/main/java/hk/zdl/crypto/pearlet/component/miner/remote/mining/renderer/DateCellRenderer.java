package hk.zdl.crypto.pearlet.component.miner.remote.mining.renderer;

import java.util.Date;

import javax.swing.table.DefaultTableCellRenderer;

import hk.zdl.crypto.pearlet.util.Util;

public class DateCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -2766480666576956825L;

	@Override
	protected void setValue(Object value) {
		if (value instanceof Long) {
			super.setValue(Util.getDateFormat().format(new Date((Long) value)));
		} else {
			super.setValue(value);
		}
	}

}
