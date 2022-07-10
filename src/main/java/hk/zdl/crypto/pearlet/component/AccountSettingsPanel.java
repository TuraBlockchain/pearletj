package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import hk.zdl.crypto.pearlet.component.account_settings.signum.CreateSignumAccount;
import hk.zdl.crypto.pearlet.component.account_settings.signum.ImportSignumAccount;
import hk.zdl.crypto.pearlet.component.account_settings.signum.WatchSignumAccount;
import hk.zdl.crypto.pearlet.component.account_settings.web3j.CreateWeb3JAccount;
import hk.zdl.crypto.pearlet.component.account_settings.web3j.ImportWeb3JAccountFromFile;
import hk.zdl.crypto.pearlet.component.account_settings.web3j.ImportWeb3JAccountFromText;
import hk.zdl.crypto.pearlet.component.account_settings.web3j.WatchWeb3JAccount;
import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.misc.AccountTableModel;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.ui.WatchAddressCellRenderer;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class AccountSettingsPanel extends JPanel {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private final AccountTableModel account_table_model = new AccountTableModel();
	private final JTable table = buildAccountTable();

	public AccountSettingsPanel() {
		super(new BorderLayout());
		EventBus.getDefault().register(account_table_model);
		var scr_1 = new JScrollPane(table);
		add(scr_1, BorderLayout.CENTER);

		var btn_panel = new JPanel(new GridBagLayout());
		var create_account_btn = new JButton("Create");
		btn_panel.add(create_account_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var import_account_btn = new JButton("Import");
		btn_panel.add(import_account_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var watch_account_btn = new JButton("Watch");
		btn_panel.add(watch_account_btn, new GridBagConstraints(0, 2, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var del_btn = new JButton("Delete");
		btn_panel.add(del_btn, new GridBagConstraints(0, 3, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));

		var create_acc_menu = new JPopupMenu();
		var create_acc_signum = new JMenuItem("Signum");
		var create_acc_rotura = new JMenuItem("Rotura");
		var create_acc_web3j = new JMenuItem("Web3j");
		Stream.of(create_acc_signum, create_acc_rotura, create_acc_web3j).forEach(create_acc_menu::add);

		create_account_btn.addActionListener(e -> create_acc_menu.show(create_account_btn, 0, 0));
		create_acc_web3j.addActionListener(e -> CreateWeb3JAccount.create_new_account_dialog(this));
		create_acc_signum.addActionListener(e -> CreateSignumAccount.create_new_account_dialog(this, CrptoNetworks.SIGNUM));
		create_acc_rotura.addActionListener(e -> CreateSignumAccount.create_new_account_dialog(this, CrptoNetworks.ROTURA));

		var import_acc_menu = new JPopupMenu();
		var import_acc_signum = new JMenuItem("Signum");
		var import_acc_rotura = new JMenuItem("Rotura");
		var import_acc_web3j = new JMenu("Web3j");
		var import_from_text = new JMenuItem("From Mnemonic ...");
		var import_from_file = new JMenuItem("From JSON File ...");
		Stream.of(import_acc_signum, import_acc_rotura,import_acc_web3j).forEach(import_acc_menu::add);
		Stream.of(import_from_text, import_from_file).forEach(import_acc_web3j::add);

		import_account_btn.addActionListener(e -> import_acc_menu.show(import_account_btn, 0, 0));
		import_acc_signum.addActionListener(e -> ImportSignumAccount.create_import_account_dialog(this, CrptoNetworks.SIGNUM));
		import_acc_rotura.addActionListener(e -> ImportSignumAccount.create_import_account_dialog(this, CrptoNetworks.ROTURA));
		import_from_text.addActionListener(e -> ImportWeb3JAccountFromText.create_import_account_dialog(this));
		import_from_file.addActionListener(e -> ImportWeb3JAccountFromFile.create_import_account_dialog(this));
		
		var watch_acc_menu = new JPopupMenu();
		var watch_acc_signum = new JMenuItem("Signum");
		var watch_acc_rotura = new JMenuItem("Rotura");
		var watch_acc_web3j = new JMenuItem("Web3j");
		Stream.of(watch_acc_signum, watch_acc_rotura,watch_acc_web3j).forEach(watch_acc_menu::add);
		watch_acc_signum.addActionListener(e -> WatchSignumAccount.create_watch_account_dialog(this, CrptoNetworks.SIGNUM));
		watch_acc_rotura.addActionListener(e -> WatchSignumAccount.create_watch_account_dialog(this, CrptoNetworks.ROTURA));
		watch_acc_web3j.addActionListener(e -> WatchWeb3JAccount.create_watch_account_dialog(this));

		watch_account_btn.addActionListener(e -> watch_acc_menu.show(watch_account_btn, 0, 0));

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
				if (mouseEvent.getClickCount() == 2 && row >= 0 & row == table.getSelectedRow()) {
					CrptoNetworks nw = CrptoNetworks.valueOf(account_table_model.getValueAt(row, 1).toString());
					Util.viewAccountDetail(nw, account_table_model.getValueAt(row, 2).toString().replace(",watch", ""));
				}
			}
		});
		
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
		table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer());
		((DefaultTableCellRenderer)table.getColumnModel().getColumn(3).getCellRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
		return table;
	}

	private static final void reload_accounts() {
		Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
	}

}