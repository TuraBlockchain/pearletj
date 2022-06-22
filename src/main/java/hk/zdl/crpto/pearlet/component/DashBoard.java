package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class DashBoard extends JPanel {

	private final JPanel token_list_inner_panel = new JPanel(new GridLayout(0, 1));
	private final JPanel token_list_panel = new JPanel(new BorderLayout());

	public DashBoard() {
		super(new BorderLayout());
		var title_font = new Font("Arial Black", Font.PLAIN, 16);
		var balance_and_tx_panel = new JPanel(new BorderLayout());
		add(token_list_panel, BorderLayout.WEST);
		add(balance_and_tx_panel, BorderLayout.CENTER);
		token_list_panel.setMinimumSize(new Dimension(200, 0));
		var label1 = new JLabel("Tokens:");
		label1.setFont(title_font);
		var panel0 = new JPanel(new FlowLayout(0));
		panel0.add(label1);
		token_list_panel.add(panel0, BorderLayout.NORTH);
		var manage_token_list_btn = new JButton("Manage Token List");
		token_list_panel.add(manage_token_list_btn, BorderLayout.SOUTH);
		var scr_pane = new JScrollPane(token_list_inner_panel);
		token_list_panel.add(scr_pane, BorderLayout.CENTER);

		var balance_panel = new JPanel(new BorderLayout());
		var label2 = new JLabel("Balance:");
		label2.setFont(title_font);
		var panel1 = new JPanel(new FlowLayout(0));
		panel1.add(label2);
		balance_panel.add(panel1, BorderLayout.NORTH);
		var balance_inner_panel = new JPanel(new FlowLayout(0));
		var label3 = new JLabel("SIG");
		var label4 = new JLabel("12345678");
		balance_inner_panel.add(label3);
		balance_inner_panel.add(label4);
		

		balance_panel.add(balance_inner_panel, BorderLayout.CENTER);
		balance_and_tx_panel.add(balance_panel,BorderLayout.NORTH);
		var table = new JTable(5,5);
		JScrollPane scrollpane = new JScrollPane(table);
		balance_and_tx_panel.add(scrollpane,BorderLayout.CENTER);

	}

}
