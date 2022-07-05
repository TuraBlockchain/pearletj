package hk.zdl.crpto.pearlet.component.dashboard.signum;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class InstantCellRenderer extends DefaultTableCellRenderer {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

	public InstantCellRenderer() {
		setHorizontalAlignment(SwingConstants.RIGHT);
	}


	@Override
	protected void setValue(Object value) {
		Instant inst = (Instant) value;
		setText(sdf.format(new Date(inst.getEpochSecond() * 1000)));
	}

}
