package hk.zdl.crypto.pearlet.component.miner.remote.mining.renderer;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.table.DefaultTableCellRenderer;

public class DateCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -2766480666576956825L;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

	@Override
	protected void setValue(Object value) {
		if (value instanceof Long) {
			super.setValue(sdf.format(new Date((Long) value)));
		} else {
			super.setValue(value);
		}
	}

}
