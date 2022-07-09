package hk.zdl.crypto.pearlet.component.dashboard.ether;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.json.JSONObject;

@SuppressWarnings("serial")
public class EtherTxIdCellRanderer extends DefaultTableCellRenderer {

	public EtherTxIdCellRanderer() {
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	@Override
	protected void setValue(Object value) {
		setText(((JSONObject)value).getString("tx_hash"));
	}

}
