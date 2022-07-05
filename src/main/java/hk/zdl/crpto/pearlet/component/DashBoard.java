package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

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
	private final TableColumnModel table_column_model = new DefaultTableColumnModel();
	private final DashboardTxProc dbtp = new DashboardTxProc(table_model);
	private final JTable table = new JTable(table_model, table_column_model);

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
		for (int i = 0; i < table_model.getColumnCount(); i++) {
			var tc = new TableColumn(i, 0);
			tc.setHeaderValue(table_model.getColumnName(i));
			table_column_model.addColumn(tc);
		}
		table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
		JScrollPane scrollpane = new JScrollPane(table);
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setShowGrid(true);
		adjust_table_width();
		balance_and_tx_panel.add(scrollpane, BorderLayout.CENTER);
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
			Util.submit(() -> {
				try {
					dbtp.update_column_model(e.network, table_column_model, address);
					dbtp.update_data(e.network, address);
					SwingUtilities.invokeLater(()->adjust_table_width());
					table.updateUI();
				} catch (Exception x) {
					Logger.getLogger(getClass().getName()).log(Level.SEVERE, x.getMessage(), x);
				}

			});
			Util.submit(() -> {
				try {
					balance_label.setText(CryptoUtil.getBalance(e.network, address).toPlainString());
				} catch (Exception x) {
					Logger.getLogger(getClass().getName()).log(Level.SEVERE, x.getMessage(), x);
				}
			});
		}
	}

	private void adjust_table_width() {
		int total_width = 0;
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 100; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}
			if (width > 300)
				width = 300;
			table_column_model.getColumn(column).setMinWidth(width);
			table_column_model.getColumn(column).setPreferredWidth(width);
			total_width +=width;
		}
		table.setPreferredScrollableViewportSize(new Dimension(total_width,500));
	}

}
