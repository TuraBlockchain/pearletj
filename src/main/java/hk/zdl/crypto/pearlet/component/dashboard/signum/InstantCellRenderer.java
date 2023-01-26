package hk.zdl.crypto.pearlet.component.dashboard.signum;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import signumj.entity.response.Transaction;

@SuppressWarnings("serial")
public class InstantCellRenderer extends DefaultTableCellRenderer {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

	public InstantCellRenderer() {
		setHorizontalAlignment(SwingConstants.RIGHT);
	}


	@Override
	protected void setValue(Object value) {
		Transaction tx = (Transaction) value;
		Date date = tx.getTimestamp().getAsDate();
		try {
			setText(sdf.format(date));
		} catch (Exception e) {
		}
	}

}
