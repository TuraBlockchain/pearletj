package hk.zdl.crypto.pearlet.component.dashboard.ether;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.json.JSONObject;

@SuppressWarnings("serial")
public class EtherAddressCellRenderer extends DefaultTableCellRenderer {

	private final String address;

	public EtherAddressCellRenderer(String address) {
		this.address = address;
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	protected void setValue(Object value) {
		JSONObject tx = (JSONObject) value;
		String from_adr = tx.getString("from_address");
		String to_adr = tx.getString("to_address");
		if (from_adr.equals(address)) {
			if (to_adr.isBlank()) {
				super.setValue("<SELF>");
			} else {
				super.setValue(to_adr);
			}
		} else {
			super.setValue(from_adr);
		}
	}

}
