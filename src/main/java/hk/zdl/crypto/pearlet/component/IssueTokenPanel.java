package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.SignumValue;

public class IssueTokenPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7832999111340079831L;
	private final Component root;
	private final CrptoNetworks nw;
	private final String account;
	private final JTextField token_name_field = new JTextField(20);
	private final JTextArea token_desc_area = new JTextArea(5, 20);
	private final JSpinner qty_spinner = new JSpinner(new SpinnerNumberModel((Number) 1L, 1L, Long.MAX_VALUE, 1L));
	private final JSpinner fee_spinner = new JSpinner(new SpinnerNumberModel((Number)1000L, 1000L, Long.MAX_VALUE, 1L));
	private final JSpinner dec_spinner = new JSpinner(new SpinnerNumberModel(0, 0, 8, 1));

	public IssueTokenPanel(Component root, CrptoNetworks nw, String account) {
		super(new GridBagLayout());
		this.root = root;
		this.nw = nw;
		this.account = account;
		init();
	}

	private void init() {
		var warning_panel = new JPanel(new GridLayout(0, 1));
		warning_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createDashedBorder(getForeground()), "Warning", TitledBorder.CENTER, TitledBorder.TOP, getFont(), getForeground()));
		warning_panel.add(new JLabel("Once submitted, you will not be able to change this information again, ever."));
		warning_panel.add(new JLabel("Make sure it is correct."));
		add(warning_panel, new GridBagConstraints(0, 0, 4, 2, 1, 0, 11, 2, new Insets(0, 0, 0, 0), 0, 0));

		var token_name_label = new JLabel("Token Name");
		add(token_name_label, new GridBagConstraints(0, 3, 1, 1, 0, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));

		add(token_name_field, new GridBagConstraints(0, 4, 4, 1, 0, 0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
		token_name_field.setToolTipText("The token name is non-unique. Should be between 3 and 10 characters long.");

		var token_desc_label = new JLabel("Description");
		add(token_desc_label, new GridBagConstraints(0, 5, 1, 1, 0, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
		var token_desc_scr = new JScrollPane(token_desc_area);
		add(token_desc_scr, new GridBagConstraints(0, 6, 4, 2, 0, 0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));

		var qty_label = new JLabel("Quantity");
		add(qty_label, new GridBagConstraints(0, 8, 2, 1, 0, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
		var dec_label = new JLabel("Decimals");
		add(dec_label, new GridBagConstraints(2, 8, 2, 1, 0, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));

		qty_spinner.setPreferredSize(new Dimension(200, 25));
		add(qty_spinner, new GridBagConstraints(0, 9, 2, 1, 1, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));

		add(dec_spinner, new GridBagConstraints(2, 9, 2, 1, 1, 0, 13, 2, new Insets(0, 0, 0, 0), 0, 0));
		dec_spinner.setToolTipText("The maximum allowed number of digits after the token quantity decimal point.");

		var fee_label = new JLabel("Fee");
		add(fee_label, new GridBagConstraints(0, 10, 4, 1, 1, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));

		String symbol = Util.default_currency_symbol.get(nw.name());
		fee_spinner.setToolTipText("The minimum fee to issue a token is 1,000 " + symbol + ".");
		var symbol_label = new JLabel(symbol);
		var fee_spinner_panel = new JPanel(new BorderLayout());
		fee_spinner_panel.add(fee_spinner, BorderLayout.CENTER);
		fee_spinner_panel.add(symbol_label, BorderLayout.EAST);
		add(fee_spinner_panel, new GridBagConstraints(0, 11, 4, 1, 1, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));

	}

	public boolean showConfirmDialog() {
		int i = JOptionPane.showConfirmDialog(root, this, "Issue Token", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (i != JOptionPane.OK_OPTION) {
			return false;
		}
		if (token_name_field.getText().isBlank()) {
			JOptionPane.showMessageDialog(root, "Token Name Required", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (token_name_field.getText().length()<3||token_name_field.getText().length()>10) {
			JOptionPane.showMessageDialog(root, "Incorrect Name, name must be in [3...10]", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (token_desc_area.getText().isBlank()) {
			JOptionPane.showMessageDialog(root, "Description is a required field.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			for (Character c : token_name_field.getText().toCharArray()) {
				if (!Character.isLetterOrDigit(c)) {
					JOptionPane.showMessageDialog(root, "Invalud Name!", "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		try {
			Record r = MyDb.getAccount(nw, account).get();
			byte[] public_key = r.getBytes("PUBLIC_KEY");
			byte[] unsigned_tx = CryptoUtil.issueAsset(nw, token_name_field.getText().trim(), token_desc_area.getText().trim(), (Long) qty_spinner.getValue(),
					SignumValue.fromSigna(new BigDecimal((Long) fee_spinner.getValue())).toNQT().longValue(), public_key);
			byte[] signed_tx = CryptoUtil.signTransaction(nw, r.getBytes("PRIVATE_KEY"), unsigned_tx);
			CryptoUtil.broadcastTransaction(nw, signed_tx);
		} catch (Throwable x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
			JOptionPane.showMessageDialog(root, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
}
