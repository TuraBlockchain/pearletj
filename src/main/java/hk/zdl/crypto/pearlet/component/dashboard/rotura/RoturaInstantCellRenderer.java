package hk.zdl.crypto.pearlet.component.dashboard.rotura;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import signumj.entity.response.Transaction;

@SuppressWarnings("serial")
public class RoturaInstantCellRenderer extends DefaultTableCellRenderer {

	/**
	 * The Burst Epoch, as a unix time
	 */
	private static final long epochBeginning;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

	static {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.set(Calendar.YEAR, 2022);
		calendar.set(Calendar.MONTH, Calendar.AUGUST);
		calendar.set(Calendar.DAY_OF_MONTH, 27);
		calendar.set(Calendar.HOUR_OF_DAY, 10);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		epochBeginning = calendar.getTimeInMillis();
	}

	public RoturaInstantCellRenderer() {
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	@Override
	protected void setValue(Object value) {
		Transaction tx = (Transaction) value;
		int burstTime = tx.getTimestamp().getTimestamp();
		Date date = new Date(epochBeginning + (burstTime * 1000L));
		try {
			setText(sdf.format(date));
		} catch (Exception e) {
		}
	}

}
