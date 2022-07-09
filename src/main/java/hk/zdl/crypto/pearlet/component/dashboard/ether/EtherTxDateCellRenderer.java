package hk.zdl.crypto.pearlet.component.dashboard.ether;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.json.JSONObject;

@SuppressWarnings("serial")
public class EtherTxDateCellRenderer extends DefaultTableCellRenderer {
	DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());

	public EtherTxDateCellRenderer() {
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	@Override
	protected void setValue(Object value) {
		String str = ((JSONObject) value).getString("block_signed_at");
		if (str.isBlank()) {
			return;
		}
		ZonedDateTime zdt = ZonedDateTime.parse(str, dtf);
		
		str = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(zdt).replace('T', ' ');
		setText(str);
	}

}
