package hk.zdl.crypto.pearlet.component.dashboard.signum;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import signumj.entity.response.Transaction;

@SuppressWarnings("serial")
public class SignumAddressCellRenderer extends DefaultTableCellRenderer {

	private final String address;
	private String prefix = "S";

	public SignumAddressCellRenderer(CryptoNetwork network, String address) {
		this.address = address;
		setHorizontalAlignment(SwingConstants.CENTER);
		try {
			prefix = CryptoUtil.getConstants(network).getString("addressPrefix");
		} catch (Exception x) {

		}
	}

	@Override
	protected void setValue(Object value) {
		Transaction tx = (Transaction) value;
		String sender = "", recp = "";
		if (tx.getSender() != null) {
			sender = prefix + "-" + tx.getSender().getRawAddress();
		}
		if (tx.getRecipient() != null) {
			recp = prefix + "-" + tx.getRecipient().getRawAddress();
		} else if (tx.getType() == 0 || (tx.getType() == 2 && tx.getSubtype() == 1)) {// Payment
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
