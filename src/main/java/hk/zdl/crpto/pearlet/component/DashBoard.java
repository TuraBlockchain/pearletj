package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crpto.pearlet.component.dashboard.DashboardTxProc;
import hk.zdl.crpto.pearlet.component.dashboard.DashboardTxTableModel;
import hk.zdl.crpto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crpto.pearlet.ui.UIUtil;
import hk.zdl.crpto.pearlet.ui.WaitLayerUI;
import hk.zdl.crpto.pearlet.util.CryptoUtil;
import hk.zdl.crpto.pearlet.util.Util;

@SuppressWarnings("serial")
public class DashBoard extends JPanel {

	private static Font title_font = new Font("Arial Black", Font.PLAIN, 16);
	private final JLayer<JPanel> jlayer = new JLayer<>();
	private final WaitLayerUI wuli = new WaitLayerUI();
	private final JPanel token_list_inner_panel = new JPanel(new GridLayout(0, 1));
	private final JPanel token_list_panel = new JPanel(new BorderLayout());
	private final JLabel currency_label = new JLabel(), balance_label = new JLabel();
	private final DashboardTxTableModel table_model = new DashboardTxTableModel();
	private final TableColumnModel table_column_model = new DefaultTableColumnModel();
	private final DashboardTxProc dbtp = new DashboardTxProc(table_model);
	private final JTable table = new JTable(table_model, table_column_model);

	private Thread tx_histroy_query = null;

	public DashBoard() {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
		add(jlayer, BorderLayout.CENTER);
		var _panel = new JPanel(new BorderLayout());
		jlayer.setView(_panel);
		jlayer.setUI(wuli);
		var balance_and_tx_panel = new JPanel(new BorderLayout());
		_panel.add(token_list_panel, BorderLayout.WEST);
		_panel.add(balance_and_tx_panel, BorderLayout.CENTER);
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
		UIUtil.adjust_table_width(table, table_column_model);
		balance_and_tx_panel.add(scrollpane, BorderLayout.CENTER);
		table_model.addTableModelListener(e -> UIUtil.adjust_table_width(table, table_column_model));
	}

	@SuppressWarnings("removal")
	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		String symbol = Util.default_currency_symbol.get(e.network.name());
		currency_label.setText(symbol);
		if (e.account.equals("null")) {
			balance_label.setText("0");
		} else {
			wuli.start();
			balance_label.setText("?");
			if (tx_histroy_query != null) {
				tx_histroy_query.stop();
			}
			tx_histroy_query = new TxTableUpdateThread(e);
			tx_histroy_query.start();
			Util.submit(() -> {
				try {
					balance_label.setText(CryptoUtil.getBalance(e.network, e.account).toPlainString());
				} catch (Exception x) {
					Logger.getLogger(getClass().getName()).log(Level.SEVERE, x.getMessage(), x);
				}
			});
		}
	}

	private final class TxTableUpdateThread extends Thread {
		private final AccountChangeEvent e;

		public TxTableUpdateThread(AccountChangeEvent e) {
			super(TxTableUpdateThread.class.getSimpleName());
			this.e = e;
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				wuli.start();
				table_model.clearData();
				dbtp.update_column_model(e.network, table_column_model, e.account);
				dbtp.update_data(e.network, e.account);
				table.updateUI();
			} catch (ThreadDeath x) {
			} catch (Exception x) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, x.getMessage(), x);
			} finally {
				wuli.stop();
			}
		}

	}

}
