package hk.zdl.crpto.pearlet.component;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.io.IOUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

public class NetworkAndAccountBar extends JPanel {

	private final JPanel left = new JPanel(new FlowLayout(0)), right = new JPanel(new FlowLayout(0));
	private final JComboBox<String> network_combobox = new JComboBox<String>(), account_combobox = new JComboBox<String>();
	private final JButton manage_network_btn = new JButton("Manage Networks"), manage_account_btn = new JButton("Manage Accounts");

	public NetworkAndAccountBar() {
		super(new GridLayout());
		init();
		
	}

	@SuppressWarnings("unchecked")
	private void init() {
		add(left);
		add(right);
		left.add(new JLabel("Network:"));
		right.add(new JLabel("Account:"));
		left.add(network_combobox);
		right.add(account_combobox);
		left.add(manage_network_btn);
		right.add(manage_account_btn);
		

		List<String> nws = Arrays.asList();
		try {
			nws = IOUtils.readLines(SettingsPanel.class.getClassLoader().getResourceAsStream("networks.txt"),"UTF-8");
		} catch (IOException e) {
		}
		network_combobox.setModel(new ListComboBoxModel<String>(nws));
		
	}

}
