package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import hk.zdl.crpto.pearlet.component.dashboard.TxProc;
import hk.zdl.crpto.pearlet.component.dashboard.TxTableModel;
import hk.zdl.crpto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crpto.pearlet.component.event.TxHistoryEvent;
import hk.zdl.crpto.pearlet.ui.UIUtil;
import hk.zdl.crpto.pearlet.ui.WaitLayerUI;
import hk.zdl.crpto.pearlet.util.CrptoNetworks;
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
	private final TxTableModel table_model = new TxTableModel();
	private final TableColumnModel table_column_model = new DefaultTableColumnModel();
	private final JTable table = new JTable(table_model, table_column_model);
	private CrptoNetworks nw;

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
		table.getTableHeader().setResizingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setShowGrid(true);
		UIUtil.adjust_table_width(table, table_column_model);
		balance_and_tx_panel.add(scrollpane, BorderLayout.CENTER);
		table_model.addTableModelListener(e -> UIUtil.adjust_table_width(table, table_column_model));
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				Point point = mouseEvent.getPoint();
				int row = table.rowAtPoint(point);
				if (mouseEvent.getClickCount() == 2 && row >= 0 & row == table.getSelectedRow()) {
					Util.viewTxDetail(nw, table_model.getValueAt(row, 0));
				}
			}
		});

	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		this.nw = e.network;
		String symbol = Util.default_currency_symbol.get(nw.name());
		currency_label.setText(symbol);
		if (e.account.equals("null")) {
			balance_label.setText("0");
		} else {
			balance_label.setText("?");
			new TxProc().update_column_model(e.network, table_column_model, e.account);
			Util.submit(() -> {
				try {
					balance_label.setText(CryptoUtil.getBalance(e.network, e.account).stripTrailingZeros().toPlainString());
				} catch (Exception x) {
					Logger.getLogger(getClass().getName()).log(Level.SEVERE, x.getMessage(), x);
				}
			});
		}
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(TxHistoryEvent<?> e) {
		switch (e.type) {
		case START:
			wuli.start();
			table_model.clearData();
			break;
		case FINISH:
			wuli.stop();
			break;
		case INSERT:
			Object o = e.data;
			table_model.insertData(new Object[] { o, o, o, o, o });
			table.updateUI();
			break;
		default:
			break;

		}
	}

}
