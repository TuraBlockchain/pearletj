package hk.zdl.crpto.pearlet.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class SendPanel extends JPanel {

	public SendPanel() {
		super(new FlowLayout());
		var panel_1 = new JPanel(new GridBagLayout());
		add(panel_1);
		var label_1 = new JLabel("Account");
		panel_1.add(label_1, newGridConst(0, 0, 3, 17));
		var label_2 = new JLabel("Balance");
		panel_1.add(label_2, newGridConst(3, 0, 1, 10));
		var label_3 = new JLabel("Token");
		panel_1.add(label_3, newGridConst(4, 0, 1, 17));
		var acc_combo_box = new JComboBox<>();
		acc_combo_box.setPreferredSize(new Dimension(300, 20));
		panel_1.add(acc_combo_box, newGridConst(0, 1, 3));
		var balance_label = new JLabel("123456");
		balance_label.setPreferredSize(new Dimension(100, 20));
//		balance_label.setBorder(BorderFactory.createLineBorder(Color.blue));
		balance_label.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_1.add(balance_label, newGridConst(3, 1, 1));
		var token_combo_box = new JComboBox<>();
		token_combo_box.setPreferredSize(new Dimension(100, 20));
		panel_1.add(token_combo_box, newGridConst(4, 1));

		var label_4 = new JLabel("Recipant");
		panel_1.add(label_4, newGridConst(0, 2, 3, 17));
		var rcv_field = new JTextField();
		rcv_field.setPreferredSize(new Dimension(500, 20));
		panel_1.add(rcv_field, newGridConst(0, 3, 5));

		var label_5 = new JLabel("Amount");
		panel_1.add(label_5, newGridConst(0, 4, 3, 17));
		var amt_field = new JTextField();
		amt_field.setPreferredSize(new Dimension(500, 20));
		panel_1.add(amt_field, newGridConst(0, 5, 5));

		var label_6 = new JLabel("Fee");
		panel_1.add(label_6, newGridConst(0, 6, 3, 17));
		var fee_field = new JTextField();
		fee_field.setPreferredSize(new Dimension(500, 20));
		panel_1.add(fee_field, newGridConst(0, 7, 5));

		var msg_chk_box = new JCheckBox("Add a Message");
		panel_1.add(msg_chk_box, newGridConst(0, 8, 3, 17));

		var label_7 = new JLabel("0/1000");
		label_7.setHorizontalAlignment(SwingConstants.RIGHT);
		label_7.setBorder(BorderFactory.createLineBorder(Color.blue));
		panel_1.add(label_7, newGridConst(3, 8, 2, 13));

		var msg_area = new JTextArea();
		var msg_scr = new JScrollPane(msg_area);
		msg_scr.setPreferredSize(new Dimension(500, 200));
		panel_1.add(msg_scr, newGridConst(0, 9, 5));

	}

	private static final GridBagConstraints newGridConst(int x, int y) {
		var a = new GridBagConstraints();
		a.gridx = x;
		a.gridy = y;
		return a;
	}

	private static final GridBagConstraints newGridConst(int x, int y, int width) {
		var a = new GridBagConstraints();
		a.gridx = x;
		a.gridy = y;
		a.gridwidth = width;
		return a;
	}

	private static final GridBagConstraints newGridConst(int x, int y, int width, int anchor) {
		var a = new GridBagConstraints();
		a.gridx = x;
		a.gridy = y;
		a.gridwidth = width;
		a.anchor = anchor;
		return a;
	}

}
