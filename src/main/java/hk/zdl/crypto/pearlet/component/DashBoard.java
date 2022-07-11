package hk.zdl.crypto.pearlet.component;

import static hk.zdl.crypto.pearlet.util.CrptoNetworks.ROTURA;
import static hk.zdl.crypto.pearlet.util.CrptoNetworks.SIGNUM;
import static hk.zdl.crypto.pearlet.util.CrptoNetworks.WEB3J;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.dashboard.TxProc;
import hk.zdl.crypto.pearlet.component.dashboard.TxTableModel;
import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.component.event.TxHistoryEvent;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.ui.WaitLayerUI;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.response.Account;
import signumj.entity.response.Asset;

@SuppressWarnings("serial")
public class DashBoard extends JPanel {

	private static Font title_font = new Font("Arial", Font.BOLD, 16);
	private static Font asset_box_font = new Font("Arial", Font.PLAIN, 16);
	private final JLayer<JPanel> jlayer = new JLayer<>();
	private final WaitLayerUI wuli = new WaitLayerUI();
	private final JList<Asset> token_list = new JList<>();
	private final JLabel currency_label = new JLabel(), balance_label = new JLabel();
	private final TxTableModel table_model = new TxTableModel();
	private final TableColumnModel table_column_model = new DefaultTableColumnModel();
	private final JTable table = new JTable(table_model, table_column_model);
	private CrptoNetworks nw;

	public DashBoard() {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
		add(jlayer, BorderLayout.CENTER);
		var panel_0 = new JPanel(new GridBagLayout());
		jlayer.setView(panel_0);
		jlayer.setUI(wuli);
		var label1 = new JLabel("Tokens:");
		panel_0.add(label1, new GridBagConstraints(0, 0, 1, 1, 0, 0, 17, 1, new Insets(0, 20, 0, 0), 0, 0));
		var scr_pane = new JScrollPane(token_list);
		scr_pane.setPreferredSize(new Dimension(200, 300));
		panel_0.add(scr_pane, new GridBagConstraints(0, 1, 1, 2, 0, 1, 17, 1, new Insets(0, 0, 0, 0), 0, 0));
		var manage_token_list_btn = new JButton("Manage Token List");
		panel_0.add(manage_token_list_btn, new GridBagConstraints(0, 3, 1, 1, 0, 0, 10, 2, new Insets(5, 5, 5, 5), 0, 0));

		var label2 = new JLabel("Balance:");
		panel_0.add(label2, new GridBagConstraints(1, 0, 1, 1, 0, 0, 17, 0, new Insets(0, 20, 0, 0), 0, 0));
		var balance_inner_panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		Stream.of(label1, label2, balance_label).forEach(o -> o.setFont(title_font));
		currency_label.setFont(new Font(Font.MONOSPACED, title_font.getStyle(), title_font.getSize()));
		Stream.of(currency_label, balance_label).forEach(balance_inner_panel::add);
		panel_0.add(balance_inner_panel, new GridBagConstraints(2, 0, 1, 1, 1, 0, 13, 0, new Insets(0, 0, 0, 0), 0, 0));
		token_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		var panel_1 = new JPanel(new BorderLayout());
		var panel_2 = new JPanel(new GridLayout(0, 1));
		var panel_3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		var panel_4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		var asset_balance_label = new JLabel();
		var asset_name_label = new JLabel();
		var asset_id_label_0 = new JLabel("asset id:");
		var asset_id_label_1 = new JLabel();
		panel_3.add(asset_balance_label);
		panel_3.add(asset_name_label);
		panel_4.add(asset_id_label_0);
		panel_4.add(asset_id_label_1);
		panel_2.add(panel_3);
		panel_2.add(panel_4);
		panel_1.add(panel_2, BorderLayout.NORTH);
		panel_2.setVisible(false);
		Stream.of(asset_balance_label,asset_name_label).forEach(o->o.setFont(asset_box_font));
		token_list.setCellRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Asset a = (Asset) value;
				super.getListCellRendererComponent(list, a.getName(), index, isSelected, cellHasFocus);
				return this;
			}
		});

		token_list.addListSelectionListener(e -> {
			if (token_list.getSelectedIndex() < 0) {
				panel_3.setVisible(false);
			} else {
				Asset a = token_list.getSelectedValuesList().get(0);
				asset_id_label_1.setText(a.getAssetId().getID());
				var desc = a.getDescription();
				panel_2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createDashedBorder(getForeground()),desc,TitledBorder.LEFT,TitledBorder.TOP,asset_box_font));
				asset_name_label.setText(a.getName());
				BigDecimal val = new BigDecimal(a.getQuantity().toNQT()).multiply(new BigDecimal(Math.pow(10, -a.getDecimals())));
				asset_balance_label.setText(val.toString());
				panel_2.setVisible(true);
			}
		});

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
		panel_0.add(panel_1, new GridBagConstraints(1, 1, 2, 3, 1, 1, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
		panel_1.add(scrollpane, BorderLayout.CENTER);
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
		if (e.account == null || e.account.isBlank()) {
			balance_label.setText("0");
		} else {
			balance_label.setText("?");
			new TxProc().update_column_model(e.network, table_column_model, e.account);
			token_list.setModel(new DefaultComboBoxModel<Asset>());
			if (Arrays.asList(ROTURA, SIGNUM).contains(nw)) {
				Util.submit(() -> {
					try {
						Account account = CryptoUtil.getAccount(nw, e.account);
						balance_label.setText(account.getBalance().toSigna().stripTrailingZeros().toPlainString());
						token_list.setListData(
								Arrays.asList(account.getAssetBalances()).stream().map(o -> CryptoUtil.getAsset(nw, o.getAssetId().toString())).collect(Collectors.toList()).toArray(new Asset[] {}));
						if (token_list.getModel().getSize() > 0) {
							token_list.setSelectedIndex(0);
						} else {
							token_list.setSelectedIndex(-1);
						}
					} catch (Exception x) {
						Logger.getLogger(getClass().getName()).log(Level.SEVERE, x.getMessage(), x);
					} finally {
						updateUI();
					}
				});
			} else if (WEB3J.equals(e.network)) {
				Util.submit(() -> {
					try {
						balance_label.setText(CryptoUtil.getBalance(e.network, e.account).stripTrailingZeros().toPlainString());
					} catch (Exception x) {
						Logger.getLogger(getClass().getName()).log(Level.SEVERE, x.getMessage(), x);
					} finally {
						updateUI();
					}
				});
			}
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
			try {
				table.updateUI();
			} catch (Exception x) {
			}
			break;
		default:
			break;

		}
	}

}
