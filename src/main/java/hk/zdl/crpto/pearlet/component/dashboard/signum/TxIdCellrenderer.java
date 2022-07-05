package hk.zdl.crpto.pearlet.component.dashboard.signum;

import javax.swing.table.DefaultTableCellRenderer;

import signumj.entity.response.Transaction;

@SuppressWarnings("serial")
public class TxIdCellrenderer extends DefaultTableCellRenderer {

	@Override
	protected void setValue(Object value) {
		Transaction tx = (Transaction) value;
		super.setValue(tx.getId());
	}

}
