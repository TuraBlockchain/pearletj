package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crpto.pearlet.component.dashboard.DashboardTxProc;
import hk.zdl.crpto.pearlet.component.dashboard.DashboardTxTableModel;
import hk.zdl.crpto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crpto.pearlet.util.CryptoUtil;
import hk.zdl.crpto.pearlet.util.Util;

@SuppressWarnings("serial")
public class DashBoard extends JPanel {

	private static Font title_font = new Font("Arial Black", Font.PLAIN, 16);
	private final JPanel token_list_inner_panel = new JPanel(new GridLayout(0, 1));
	private final JPanel token_list_panel = new JPanel(new BorderLayout());
	private final JLabel currency_label = new JLabel(), balance_label = new JLabel();
	private final DashboardTxTableModel table_model = new DashboardTxTableModel();
	private final DashboardTxProc dbtp = new DashboardTxProc(table_model);

	public DashBoard() {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
		var balance_and_tx_panel = new JPanel(new BorderLayout());
		add(token_list_panel, BorderLayout.WEST);
		add(balance_and_tx_panel, BorderLayout.CENTER);
		token_list_panel.setMinimumSize(new Dimension(200, 0));
		var label1 = new JLabel("Tokens:");
		var panel0 = new JPanel(new FlowLayout(0));
		panel0.add(label1);
		token_list_panel.add(panel0, BorderLayout.NORTH);
		var manage_token_list_btn = new JButton("Manage Token List");
		token_list_panel.add(manage_token_list_btn, BorderLayout.SOUTH);
		var scr_pane = new JScrollPane(token_list_inner_panel);
		token_list_panel.add(scr_pane, BorderLayout.CENTER);

		var balance_panel = new JPanel(new BorderLayout());
		var label2 = new JLabel("Balance:");
		var panel1 = new JPanel(new FlowLayout(0));
		panel1.add(label2);
		balance_panel.add(panel1, BorderLayout.WEST);
		var balance_inner_panel = new JPanel(new FlowLayout(0));
		Stream.of(label1, label2, currency_label, balance_label).forEach(o -> o.setFont(title_font));
		Stream.of(currency_label, balance_label).forEach(balance_inner_panel::add);

		balance_panel.add(balance_inner_panel, BorderLayout.EAST);
		balance_and_tx_panel.add(balance_panel, BorderLayout.NORTH);
		var table = new JTable(table_model);
		JScrollPane scrollpane = new JScrollPane(table);
		balance_and_tx_panel.add(scrollpane, BorderLayout.CENTER);
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setShowGrid(true);

	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		String symbol = Util.default_currency_symbol.get(e.network.name());
		String address = e.account;
		currency_label.setText(symbol);
		if (address.equals("null")) {
			balance_label.setText("0");
		} else {
			balance_label.setText("?");
			Util.submit(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					try {
						dbtp.update(e.network, address);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			});
			Util.submit(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					balance_label.setText(CryptoUtil.getBalance(e.network, address).toPlainString());
					return null;
				}
			});
		}
	}

}
