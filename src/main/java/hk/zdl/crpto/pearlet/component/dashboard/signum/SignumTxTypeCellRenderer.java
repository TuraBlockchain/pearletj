package hk.zdl.crpto.pearlet.component.dashboard.signum;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class SignumTxTypeCellRenderer extends DefaultTableCellRenderer {
	
	private static final Map<Integer,String> map = new TreeMap<>();
	
	static {
		map.put(0, "Payment");
		map.put(20, "Mining");
		map.put(21, "Escrow");
		map.put(22, "Auto Transcation");
	}

	public SignumTxTypeCellRenderer() {
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	@Override
	protected void setValue(Object value) {
		value = map.get(value);
		if(value == null) {
			value = "Unknown";
		}
		super.setValue(value);
	}

}
