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
		case 0:
			switch (tx.getSubtype()) {
			case 0:
				value = "Ordinary Payment";
				break;
			case 1:
				value = "Multi-Out Payment";
				break;
			case 2:
				value = "Multi-Out Same Payment";
				break;
			}
			break;
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
			case 6:
				value = "Alias Sell";
				break;
			case 7:
				value = "Alias Buy";
				break;
			}
			break;
		case 2:
			switch (tx.getSubtype()) {
			case 0:
				value = "Token Issuance";
				break;
			case 1:
			case 6:
				value = "Token Transfer";
				break;
			case 2:
				value = "Ask Order Placement";
				break;
			case 3:
				value = "Bid Order Placement";
				break;
			case 4:
				value = "Ask Order Cancellation";
				break;
			case 5:
				value = "Bid Order Cancellation";
				break;
			case 7:
				value = "Asset Add Treasury Account";
				break;
			case 8:
				value = "Asset Distribute to Holders";
				break;
			case 9:
				value = "Asset Multi Transfer";
				break;
			}
			break;
		case 3:
			switch (tx.getSubtype()) {
			case 0:
				value = "Marketplace Listing";
				break;
			case 1:
				value = "Marketplace Removal";
				break;
			case 2:
				value = "Marketplace Price Change";
				break;
			case 3:
				value = "Marketplace Quantity Change";
				break;
			case 4:
				value = "Marketplace Purchase";
				break;
			case 5:
				value = "Marketplace Delivery";
				break;
			case 6:
				value = "Marketplace Feedback";
				break;
			case 7:
				value = "Marketplace Fefund";
				break;
			}
			break;
		case 4:
			switch (tx.getSubtype()) {
			case 0:
				value = "Balance Leasing";
				break;
			}
			break;
		case 20:
			switch (tx.getSubtype()) {
			case 0:
				value = "Reward Recipient Assignment";
				break;
			case 1:
				value = "Add Commitment";
				break;
			case 2:
				value = "Revoke Commitment";
				break;
			}
			break;
		case 21:
			switch (tx.getSubtype()) {
			case 0:
				value = "Escrow Creation";
				break;
			case 1:
				value = "Escrow Signing";
				break;
			case 2:
				value = "Escrow Result";
				break;
			case 3:
				value = "Subscription Subscribe";
				break;
			case 4:
				value = "Subscription Cancel";
				break;
			case 5:
				value = "Subscription Payment";
				break;
			}
			break;
		case 22:
			switch (tx.getSubtype()) {
			case 0:
				value = "AT Creation";
				break;
			case 1:
				value = "AT Payment";
				break;
			}
			break;
		}
		super.setValue(value);
	}

}
