package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ResourceBundle;
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

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.lock.CryptoAccount;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class IssueTokenPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7832999111340079831L;
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	private final Component root;
	private final CryptoNetwork nw;
	private final String account;
	private final JTextField token_name_field = new JTextField(20);
	private final JTextArea token_desc_area = new JTextArea(5, 20);
	private final JSpinner qty_spinner = new JSpinner(new SpinnerNumberModel((Number) 1L, 1L, Long.MAX_VALUE, 1L));
	private final JSpinner fee_spinner = new JSpinner(new SpinnerNumberModel((Number) 1000L, 1000L, Long.MAX_VALUE, 1L));
	private final JSpinner dec_spinner = new JSpinner(new SpinnerNumberModel(0, 0, 8, 1));

	public IssueTokenPanel(Component root, CryptoNetwork nw, String account) {
		super(new GridBagLayout());
		this.root = root;
		this.nw = nw;
		this.account = account;
		init();
	}

	private void init() {
		var warning_panel = new JPanel(new GridLayout(0, 1));
		warning_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createDashedBorder(getForeground()), rsc_bdl.getString("GENERAL.WARNING"), TitledBorder.CENTER, TitledBorder.TOP,
				getFont(), getForeground()));
		warning_panel.add(new JLabel(rsc_bdl.getString("ISSUE_TOKEN.PANEL_TEXT_1")));
		warning_panel.add(new JLabel(rsc_bdl.getString("ISSUE_TOKEN.PANEL_TEXT_2")));
		add(warning_panel, new GridBagConstraints(0, 0, 4, 2, 1, 0, 11, 2, new Insets(0, 0, 0, 0), 0, 0));

		var token_name_label = new JLabel(rsc_bdl.getString("ISSUE_TOKEN.PANEL_TOKEN_NAME"));
		add(token_name_label, new GridBagConstraints(0, 3, 1, 1, 0, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));

		add(token_name_field, new GridBagConstraints(0, 4, 4, 1, 0, 0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
		token_name_field.setToolTipText(rsc_bdl.getString("ISSUE_TOKEN.PANEL_TOKEN_NAME_TOOL_TIP"));

		var token_desc_label = new JLabel(rsc_bdl.getString("GENERAL.DESC"));
		add(token_desc_label, new GridBagConstraints(0, 5, 1, 1, 0, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
		var token_desc_scr = new JScrollPane(token_desc_area);
		add(token_desc_scr, new GridBagConstraints(0, 6, 4, 2, 0, 0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));

		var qty_label = new JLabel(rsc_bdl.getString("ISSUE_TOKEN.PANEL_QTY"));
		add(qty_label, new GridBagConstraints(0, 8, 2, 1, 0, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
		var dec_label = new JLabel(rsc_bdl.getString("ISSUE_TOKEN.PANEL_DSC"));
		add(dec_label, new GridBagConstraints(2, 8, 2, 1, 0, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));

		qty_spinner.setPreferredSize(new Dimension(200, 25));
		add(qty_spinner, new GridBagConstraints(0, 9, 2, 1, 1, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));

		add(dec_spinner, new GridBagConstraints(2, 9, 2, 1, 1, 0, 13, 2, new Insets(0, 0, 0, 0), 0, 0));
		dec_spinner.setToolTipText(rsc_bdl.getString("ISSUE_TOKEN.PANEL_DSC_TOOL_TIP"));

		var fee_label = new JLabel(rsc_bdl.getString("GENERAL.FEE"));
		add(fee_label, new GridBagConstraints(0, 10, 4, 1, 1, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));

		var symbol = "";
		fee_spinner.setToolTipText(MessageFormat.format(rsc_bdl.getString("ISSUE_TOKEN.PANEL_MIN_FEE_TOOL_TIP"), "1000", symbol));
		var symbol_label = new JLabel(symbol);
		var fee_spinner_panel = new JPanel(new BorderLayout());
		fee_spinner_panel.add(fee_spinner, BorderLayout.CENTER);
		fee_spinner_panel.add(symbol_label, BorderLayout.EAST);
		add(fee_spinner_panel, new GridBagConstraints(0, 11, 4, 1, 1, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));

	}

	public boolean showConfirmDialog() {
		int i = JOptionPane.showConfirmDialog(root, this, rsc_bdl.getString("ISSUE_TOKEN.PANEL_TITLE"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (i != JOptionPane.OK_OPTION) {
			return false;
		}
		if (token_name_field.getText().isBlank()) {
			JOptionPane.showMessageDialog(root, rsc_bdl.getString("ISSUE_TOKEN.NAME_REQUIRED"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (token_name_field.getText().length() < 3 || token_name_field.getText().length() > 10) {
			JOptionPane.showMessageDialog(root, rsc_bdl.getString("ISSUE_TOKEN.NAME_INCOORECT"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (token_desc_area.getText().isBlank()) {
			JOptionPane.showMessageDialog(root, rsc_bdl.getString("ISSUE_TOKEN.DESC_MISSING"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			for (Character c : token_name_field.getText().toCharArray()) {
				if (!Character.isLetterOrDigit(c)) {
					JOptionPane.showMessageDialog(root, rsc_bdl.getString("ISSUE_TOKEN.NAME_INVALID"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		try {
			var r = CryptoAccount.getAccount(nw, account).get();
			byte[] public_key = r.getPublicKey();
			byte[] private_key = r.getPrivateKey();
			byte[] unsigned_tx = CryptoUtil.issueAsset(nw, token_name_field.getText().trim(), token_desc_area.getText().trim(), (Long) qty_spinner.getValue(),
					CryptoUtil.toSignumValue(nw, new BigDecimal((Long) fee_spinner.getValue())).toNQT().longValue(), public_key);
			byte[] signed_tx = CryptoUtil.signTransaction(nw, private_key, unsigned_tx);
			CryptoUtil.broadcastTransaction(nw, signed_tx);
		} catch (Throwable x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
			JOptionPane.showMessageDialog(root, x.getMessage(), x.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
}
