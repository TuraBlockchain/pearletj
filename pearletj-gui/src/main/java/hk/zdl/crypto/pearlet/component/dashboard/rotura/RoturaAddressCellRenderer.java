package hk.zdl.crypto.pearlet.component.dashboard.rotura;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import hk.zdl.crypto.pearlet.ds.RoturaAddress;
import signumj.entity.response.Transaction;

@SuppressWarnings("serial")
public class RoturaAddressCellRenderer extends DefaultTableCellRenderer {

	private final String address;

	public RoturaAddressCellRenderer(String address) {
		this.address = address;
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	protected void setValue(Object value) {
		Transaction tx = (Transaction) value;
		String sender = "", recp = "";
		if (tx.getSender() != null) {
			sender = RoturaAddress.prefix + "-" + tx.getSender().getRawAddress();
		}
		if (tx.getRecipient() != null) {
			recp = RoturaAddress.prefix + "-" + tx.getRecipient().getRawAddress();
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
