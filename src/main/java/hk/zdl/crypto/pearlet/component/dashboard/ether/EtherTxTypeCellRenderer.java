package hk.zdl.crypto.pearlet.component.dashboard.ether;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class EtherTxTypeCellRenderer extends DefaultTableCellRenderer {

	public EtherTxTypeCellRenderer() {
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	protected void setValue(Object value) {
		setText("Payment");
	}

}
