package hk.zdl.crypto.pearlet.component.dashboard.signum;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import signumj.entity.response.Transaction;

@SuppressWarnings("serial")
public class SignumTxTypeCellRenderer extends DefaultTableCellRenderer {

	private static final Map<Integer, String> map = new TreeMap<>();

	static {
		map.put(0, "Payment");
		map.put(1, "Message");
		map.put(2, "Token");
		map.put(20, "Mining");
		map.put(21, "Escrow");
		map.put(22, "Auto Transcation");
	}

	public SignumTxTypeCellRenderer() {
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	protected void setValue(Object value) {
		Transaction tx = (Transaction) value;
		value = map.get(tx.getType());
		if (value == null) {
			value = "Unknown";
		}
		switch (tx.getType()) {
		case 1:
			switch (tx.getSubtype()) {
			case 0:
				value = "Message";
				break;
			case 1:
				value = "Alias Assignment";
				break;
			case 5:
				value = "Account Info";
				break;
			}
			break;
		case 2:
			switch (tx.getSubtype()) {
			case 0:
				value = "Token Issuance";
				break;
			case 1:
				value = "Token Transfer";
				break;
			}
			break;
		}
		super.setValue(value);
	}

}
