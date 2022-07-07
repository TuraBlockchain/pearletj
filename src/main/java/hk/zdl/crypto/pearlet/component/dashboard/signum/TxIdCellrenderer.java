package hk.zdl.crypto.pearlet.component.dashboard.signum;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import signumj.entity.response.Transaction;

@SuppressWarnings("serial")
public class TxIdCellrenderer extends DefaultTableCellRenderer {

	public TxIdCellrenderer() {
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	@Override
	protected void setValue(Object value) {
		Transaction tx = (Transaction) value;
		super.setValue(tx.getId());
	}

}
