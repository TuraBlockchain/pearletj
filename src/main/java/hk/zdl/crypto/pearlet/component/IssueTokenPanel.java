package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;

public class IssueTokenPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7832999111340079831L;
	private final Component root;
	private final CrptoNetworks nw;
	private final String account;

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
		var token_name_field = new JTextField(20);
		add(token_name_field, new GridBagConstraints(0, 4, 4, 1, 0, 0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
		token_name_field.setToolTipText("The token name is non-unique. Should be between 3 and 10 characters long.");
		

		var token_desc_label = new JLabel("Description");
		add(token_desc_label, new GridBagConstraints(0, 5, 1, 1, 0, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
		var token_desc_area = new JTextArea(5,20);
		var token_desc_scr = new JScrollPane(token_desc_area);
		add(token_desc_scr, new GridBagConstraints(0, 6, 4, 2, 0, 0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
		
		var qty_label = new JLabel("Quantity");
		add(qty_label, new GridBagConstraints(0, 8, 2, 1, 0, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
		var dec_label = new JLabel("Decimals");
		add(dec_label, new GridBagConstraints(2, 8, 2, 1, 0, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
		
		var qty_spinner = new JSpinner(new SpinnerNumberModel((Number)1L,1L,Long.MAX_VALUE,1L));
		qty_spinner.setPreferredSize(new Dimension(200,25));
		add(qty_spinner, new GridBagConstraints(0, 9, 2, 1, 1, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
		
		var dec_spinner = new JSpinner(new SpinnerNumberModel(0,0,8,1));
		add(dec_spinner, new GridBagConstraints(2, 9, 2, 1, 1, 0, 13, 2, new Insets(0, 0, 0, 0), 0, 0));
		dec_spinner.setToolTipText("The maximum allowed number of digits after the token quantity decimal point.");

		var fee_label = new JLabel("Fee");
		add(fee_label, new GridBagConstraints(0, 10, 4, 1, 1, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
		
		String symbol = Util.default_currency_symbol.get(nw.name());
		var fee_spinner = new JSpinner(new SpinnerNumberModel(1000,1000,Integer.MAX_VALUE,1));
		fee_spinner.setToolTipText("The minimum fee to issue a token is 1,000 "+symbol+".");
		var symbol_label = new JLabel(symbol);
		var fee_spinner_panel = new JPanel(new BorderLayout());
		fee_spinner_panel.add(fee_spinner,BorderLayout.CENTER);
		fee_spinner_panel.add(symbol_label,BorderLayout.EAST);
		add(fee_spinner_panel, new GridBagConstraints(0, 11, 4, 1, 1, 0, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
		
	}

	public boolean showConfirmDialog() {
		int i = JOptionPane.showConfirmDialog(root, this, "Issue Token", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		return i == JOptionPane.OK_OPTION;
	}
}
