package hk.zdl.crypto.pearlet.component.dashboard.signum;

import java.awt.Color;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.Optional;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.jthemedetecor.OsThemeDetector;

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import signumj.entity.SignumValue;
import signumj.entity.response.Transaction;
import signumj.response.attachment.CommitmentAddAttachment;
import signumj.response.attachment.CommitmentRemoveAttachment;

@SuppressWarnings("serial")
public class SignumValueCellRenderer extends DefaultTableCellRenderer {

	private static final OsThemeDetector otd = OsThemeDetector.getDetector();
	private static final Color my_cyan = new Color(0, 175, 175), my_green = new Color(175, 255, 175);
	private final String address;
	private Optional<Integer> decimalPlaces = Optional.empty();

	public SignumValueCellRenderer(CryptoNetwork network, String address) {
		this.address = address;
		setHorizontalAlignment(SwingConstants.RIGHT);
		try {
			int i = CryptoUtil.getConstants(network).getInt("decimalPlaces");
			decimalPlaces = Optional.of(i);
		} catch (Exception x) {

		}
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		boolean isDark = otd.isDark();
		setBackground(isDark ? darker(my_cyan) : my_cyan);
		if (!isSelected) {
			Transaction tx = (Transaction) value;
			if (tx.getType() == 0 || (tx.getType() == 2 && tx.getSubtype() == 1)) {// Payment
				if (tx.getRecipient() != null && tx.getRecipient().getRawAddress().equals(address.substring(address.indexOf('-') + 1))) {
					setBackground(isDark ? darker(my_green) : my_green);
				} else {
					setBackground(isDark ? darker(Color.pink) : Color.pink);
				}
			}
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

	@Override
	protected void setValue(Object value) {
		Transaction tx = (Transaction) value;
		SignumValue val = tx.getAmount();
		if (tx.getType() == 20) {
			if (tx.getSubtype() == 1) {
				val = SignumValue.fromNQT(((CommitmentAddAttachment) tx.getAttachment()).getAmountNQT());
			} else if (tx.getSubtype() == 2) {
				val = SignumValue.fromNQT(((CommitmentRemoveAttachment) tx.getAttachment()).getAmountNQT());
			}
		}
		if (decimalPlaces.isPresent()) {
			super.setValue(new BigDecimal(val.toNQT(), decimalPlaces.get()).toPlainString());
		} else {
			super.setValue(val.toSigna().toPlainString());
		}
	}

	private static final Color darker(Color c) {
		float factor = 0.4f;
		return new Color(Math.max((int) (c.getRed() * factor), 0), Math.max((int) (c.getGreen() * factor), 0), Math.max((int) (c.getBlue() * factor), 0), c.getAlpha());
	}

}
