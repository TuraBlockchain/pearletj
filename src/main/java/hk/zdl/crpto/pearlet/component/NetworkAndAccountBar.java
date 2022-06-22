package hk.zdl.crpto.pearlet.component;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class NetworkAndAccountBar extends JPanel {

	private final JPanel left = new JPanel(new FlowLayout(0)), right = new JPanel(new FlowLayout(0));
	private final JComboBox network_combobox = new JComboBox(), account_combobox = new JComboBox();
	private final JButton manage_network_btn = new JButton("Manage Networks"), manage_account_btn = new JButton("Manage Accounts");

	public NetworkAndAccountBar() {
		super(new GridLayout(1, 2));
		add(left);
		add(right);
		left.add(new JLabel("Network:"));
		right.add(new JLabel("Account:"));
		left.add(network_combobox);
		right.add(account_combobox);
		left.add(manage_network_btn);
		right.add(manage_account_btn);
	}

}
