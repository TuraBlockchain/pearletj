package hk.zdl.crpto.pearlet.component.dashboard.signum;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import signumj.entity.response.Transaction;

@SuppressWarnings("serial")
public class SignumAddressCellRenderer extends DefaultTableCellRenderer {

	private final String address;

	public SignumAddressCellRenderer(String address) {
		this.address = address;
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	@Override
	protected void setValue(Object value) {
		Transaction tx = (Transaction) value;
		String sender = tx.getSender().getFullAddress();
		String recp = tx.getRecipient().getFullAddress();
		if (sender.equals(address)) {
			super.setValue(recp);
		} else {
			super.setValue(sender);
		}
	}

}
