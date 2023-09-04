package hk.zdl.crypto.pearlet.component.blocks;

import java.math.BigDecimal;
import java.util.Optional;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.util.CryptoUtil;

@SuppressWarnings("serial")
public class SignumValueCellRenderer extends DefaultTableCellRenderer {

	private Optional<Integer> decimalPlaces = Optional.empty();

	public SignumValueCellRenderer(CryptoNetwork network) {
		setHorizontalAlignment(SwingConstants.RIGHT);
		try {
			int i = CryptoUtil.getConstants(network).getInt("decimalPlaces");
			decimalPlaces = Optional.of(i);
		} catch (Exception x) {

		}
	}

	@Override
	protected void setValue(Object value) {
		var val = new BigDecimal(value.toString()).movePointLeft(decimalPlaces.get());
		setText(val.toPlainString());
	}

}
