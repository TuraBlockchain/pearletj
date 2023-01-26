package hk.zdl.crypto.pearlet.component.dashboard.signum;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import signumj.entity.response.Transaction;

@SuppressWarnings("serial")
public class SignumAddressCellRenderer extends DefaultTableCellRenderer {

	private final String address;

	public SignumAddressCellRenderer(String address) {
		this.address = address;
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	protected void setValue(Object value) {
		Transaction tx = (Transaction) value;
		String sender = "", recp = "";
		if (tx.getSender() != null) {
			sender = tx.getSender().getFullAddress();
		}
		if (tx.getRecipient() != null) {
			recp = tx.getRecipient().getFullAddress();
		} else {
			recp = "Burning Address ";
		}
		if (sender.equals(address)) {
			if (recp.isBlank()) {
				super.setValue("<SELF>");
			} else {
				super.setValue(recp);
			}
		} else {
			super.setValue(sender);
		}
	}

}
