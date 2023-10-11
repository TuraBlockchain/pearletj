package hk.zdl.crypto.pearlet.component.settings;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.account_settings.CreateAccount;
import hk.zdl.crypto.pearlet.component.account_settings.ExportAccountTable;
import hk.zdl.crypto.pearlet.component.account_settings.burst.ImportBurstAccount;
import hk.zdl.crypto.pearlet.component.account_settings.burst.WatchBurstAccount;
import hk.zdl.crypto.pearlet.component.account_settings.web3j.WatchWeb3JAccount;
import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.component.event.NetworkChangeEvent;
import hk.zdl.crypto.pearlet.component.event.SetNAABarEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.misc.AccountTableModel;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.TxAmountCellRenderer;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.ui.WatchAddressCellRenderer;
import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class AccountSettingsPanel extends JPanel {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private final AccountTableModel account_table_model = new AccountTableModel();
	private final JTable table = buildAccountTable();
	private CryptoNetwork nw;
	private JButton create_account_btn, import_account_btn, export_account_btn, watch_account_btn, del_btn;

	public AccountSettingsPanel() {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
		EventBus.getDefault().register(account_table_model);
		add(new JScrollPane(table), BorderLayout.CENTER);

		table.getColumnModel().removeColumn(table.getColumnModel().getColumn(0));
		var btn_panel = new JPanel(new GridBagLayout());
		create_account_btn = new JButton("Create");
		import_account_btn = new JButton("Import");
		export_account_btn = new JButton("Export");
		watch_account_btn = new JButton("Watch");
		del_btn = new JButton("Delete");
		btn_panel.add(create_account_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(import_account_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(export_account_btn, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(watch_account_btn, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(del_btn, new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

		create_account_btn.addActionListener(e -> {
			if (nw == null) {
				return;
			}
			var nws = MyDb.get_networks();
			if (nws.isEmpty()) {
				return;
			} else if (nws.size() == 1) {
				CreateAccount.create_new_account_dialog(this, nw);
			} else {
				var menu = new JPopupMenu();
				for (var n : nws) {
					menu.add(n.getName()).addActionListener(a -> CreateAccount.create_new_account_dialog(this, n));
				}
				menu.show(create_account_btn, 0, 0);
			}
		});
		import_account_btn.addActionListener(e -> {
			if (nw == null) {
				return;
			}
			if (UIUtil.isAltDown(e)) {
				ImportBurstAccount.batch_import(this, nw);
			} else {
				var nws = MyDb.get_networks();
				if (nws.isEmpty()) {
					return;
				} else if (nws.size() == 1 && nw.isBurst()) {
					ImportBurstAccount.create_import_account_dialog(this, nw);
				} else {
					var menu = new JPopupMenu();
					for (var n : nws) {
						if (n.isBurst()) {
							var item = new JMenuItem(n.getName());
							item.addActionListener(a -> ImportBurstAccount.create_import_account_dialog(this, n));
							menu.add(item);
						} else if (n.isWeb3J()) {
							var import_acc_web3j = new JMenu(n.getName());
							var import_from_prik = new JMenuItem("From Private Key ...");
							var import_from_mnic = new JMenuItem("From Mnemonic ...");
							var import_from_file = new JMenuItem("From JSON File ...");
							Stream.of(import_from_prik, import_from_mnic, import_from_file).forEach(import_acc_web3j::add);
							menu.add(import_acc_web3j);
						}
					}
					menu.show(import_account_btn, 0, 0);
				}
			}
		});

		var export_acc_menu = new JPopupMenu();
//		var export_acc_item = new JMenuItem("Current account...");
//		export_acc_item.setEnabled(false);
		var export_table_item = new JMenu("Table As ...");
		var export_csv_item = new JMenuItem("Comma-separated values (.CSV)");
		export_table_item.add(export_csv_item);
		export_acc_menu.add(export_table_item);
		export_account_btn.addActionListener(e -> export_acc_menu.show(export_account_btn, 0, 0));
		export_csv_item.addActionListener(e -> ExportAccountTable.export_csv(this, account_table_model));

		watch_account_btn.addActionListener(e -> {
			if (nw == null) {
				return;
			}
			var nws = MyDb.get_networks();
			if (nws.isEmpty()) {
				return;
			} else if (nws.size() == 1) {
				if (nw.isBurst()) {
					WatchBurstAccount.create_watch_account_dialog(this, nw);
				} else if (nw.isWeb3J()) {
					WatchWeb3JAccount.create_watch_account_dialog(this, nw);
				}
			} else {
				var menu = new JPopupMenu();
				for (var n : nws) {
					if (n.isBurst()) {
						var item = new JMenuItem(n.getName());
						item.addActionListener(a -> WatchBurstAccount.create_watch_account_dialog(this, n));
						menu.add(item);
					} else if (n.isWeb3J()) {
						var item = new JMenuItem(n.getName());
						item.addActionListener(a -> WatchWeb3JAccount.create_watch_account_dialog(this, n));
						menu.add(item);
					}
				}
				menu.show(watch_account_btn, 0, 0);
			}
		});

		del_btn.addActionListener(e -> Util.submit(() -> {
			int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			int i = JOptionPane.showConfirmDialog(getRootPane(), "Are you sure to delete this?", "", JOptionPane.YES_NO_OPTION);
			if (i == 0) {
				int id = Integer.parseInt(account_table_model.getValueAt(row, 0).toString());
				MyDb.deleteAccount(id);
				reload_accounts();
			}
		}));

		reload_accounts();

		var panel_1 = new JPanel(new FlowLayout(1, 0, 0));
		panel_1.add(btn_panel);
		add(panel_1, BorderLayout.EAST);
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent e) {
				SwingUtilities.invokeLater(() -> UIUtil.adjust_table_width(table, table.getColumnModel()));
			}

		});
		table.getModel().addTableModelListener((e) -> SwingUtilities.invokeLater(() -> UIUtil.adjust_table_width(table, table.getColumnModel())));
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				Point point = mouseEvent.getPoint();
				int row = table.rowAtPoint(point);
				if (row >= 0 & row == table.getSelectedRow()) {
					var nw = (CryptoNetwork) account_table_model.getValueAt(row, 1);
					var adr = account_table_model.getValueAt(row, 2).toString().replace(",watch", "");
					if (mouseEvent.getClickCount() == 1) {
						if (new KeyEvent(AccountSettingsPanel.this, 0, 0, mouseEvent.getModifiersEx(), 0, ' ').isAltDown()) {
							EventBus.getDefault().post(new SetNAABarEvent(nw, adr));
						}
					} else if (mouseEvent.getClickCount() == 2) {
						try {
							Util.viewAccountDetail(nw, adr);
						} catch (Exception x) {
							Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
						}
					}
				}
			}
		});

		table.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					del_btn.doClick();
				}
			}
		});
		onMessage(new NetworkChangeEvent());
	}

	private final JTable buildAccountTable() {
		var table = new JTable(account_table_model);
		table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setShowGrid(true);
		table.getColumnModel().getColumn(2).setCellRenderer(new WatchAddressCellRenderer());
		table.getColumnModel().getColumn(3).setCellRenderer(new TxAmountCellRenderer());
		table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
		((DefaultTableCellRenderer) table.getColumnModel().getColumn(0).getCellRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
		return table;
	}

	private static final void reload_accounts() {
		EventBus.getDefault().post(new AccountListUpdateEvent());
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		this.nw = e.network;
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(NetworkChangeEvent e) {
		var l = MyDb.get_networks().stream().filter(o -> o.isBurst() || o.isWeb3J()).toList();
		Stream.of(create_account_btn, import_account_btn, watch_account_btn).forEach(o -> o.setEnabled(!l.isEmpty()));
	}

}
