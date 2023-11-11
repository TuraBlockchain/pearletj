package hk.zdl.crypto.pearlet.component.blocks;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class InstantCellRenderer extends DefaultTableCellRenderer {

	private long epochBeginning = 0;

	public InstantCellRenderer(CryptoNetwork network) {
		setHorizontalAlignment(SwingConstants.RIGHT);
		try {
			var epoch_str = CryptoUtil.getConstants(network).getString("epoch");
			epochBeginning = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ssX").parse(epoch_str).getTime();
		} catch (Exception e) {
		}
	}

	@Override
	protected void setValue(Object value) {
		if (value != null) {
			long burstTime = Long.valueOf(value.toString());
			Date date = new Date(epochBeginning + (burstTime * 1000L));
			setText(Util.getDateFormat().format(date));
		} else {
			setText("");
		}
	}
}
