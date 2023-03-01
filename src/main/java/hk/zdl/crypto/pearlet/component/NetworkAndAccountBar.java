package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.component.event.NetworkChangeEvent;
import hk.zdl.crypto.pearlet.component.event.SetNAABarEvent;
import hk.zdl.crypto.pearlet.component.event.SettingsPanelEvent;
import hk.zdl.crypto.pearlet.ds.AccountComboboxEntry;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.AccountComboboxRenderer;
import hk.zdl.crypto.pearlet.ui.MyListComboBoxModel;
import hk.zdl.crypto.pearlet.ui.UIUtil;

@SuppressWarnings("serial")
public class NetworkAndAccountBar extends JPanel {

	private final JPanel left = new JPanel(new FlowLayout()), right = new JPanel(new FlowLayout());
	private final JComboBox<CryptoNetwork> network_combobox = new JComboBox<>();
	private final JComboBox<AccountComboboxEntry> account_combobox = new JComboBox<>();

	public NetworkAndAccountBar() {
		super(new BorderLayout());
		init();
		EventBus.getDefault().register(this);
	}

	@SuppressWarnings("unchecked")
	private void init() {
		add(left, BorderLayout.WEST);
		add(right, BorderLayout.EAST);
		left.add(new JLabel("Network:"));
		right.add(new JLabel("Account:"));
		left.add(network_combobox);
		right.add(account_combobox);
		Stream.of(network_combobox, account_combobox).forEach(o -> o.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize())));

		Icon btn_icon = null;
		btn_icon = UIUtil.getStretchIcon("toolbar/" + "screwdriver-wrench-solid.svg", 24, 24);
		var manage_network_btn = new JButton(btn_icon);
		var manage_account_btn = new JButton(btn_icon);
		manage_network_btn.setToolTipText("Manage Networks");
		manage_account_btn.setToolTipText("Manage Accounts");

		left.add(manage_network_btn);
		right.add(manage_account_btn);

		network_combobox.setModel(new ListComboBoxModel<>(MyDb.get_networks()));
		network_combobox.addActionListener(e -> update_account_combobox());
		network_combobox.setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				var o = (CryptoNetwork) value;
				return super.getListCellRendererComponent(list, o==null?"":o.getName(), index, isSelected, cellHasFocus);
			}
		});

		manage_network_btn.addActionListener(e -> EventBus.getDefault().post(new SettingsPanelEvent(SettingsPanelEvent.NET)));
		manage_account_btn.addActionListener(e -> EventBus.getDefault().post(new SettingsPanelEvent(SettingsPanelEvent.ACC)));

		account_combobox.addActionListener(e -> update_current_account());
		account_combobox.setRenderer(new AccountComboboxRenderer());
	}

	private final void update_account_combobox() {
		refresh_account_combobox();
		update_current_account();
	}

	@SuppressWarnings("unchecked")
	private void refresh_account_combobox() {
		var nw = (CryptoNetwork) network_combobox.getSelectedItem();
		var l = MyDb.getAccounts().stream().filter(o -> o.getInt("NWID") !=null).filter(o -> o.getInt("NWID") == nw.getId()).map(o -> o.getStr("ADDRESS")).map(o -> new AccountComboboxEntry(nw, o, null)).toList();
		account_combobox.setModel(new MyListComboBoxModel<>(new ArrayList<>(l)));
		account_combobox.setEnabled(!l.isEmpty());
	}

	private void update_current_account() {
		var nw = (CryptoNetwork) network_combobox.getSelectedItem();
		String str = null;
		var acc = account_combobox.getSelectedItem();
		if (acc == null) {
			EventBus.getDefault().post(new AccountChangeEvent(nw, null));
		} else {
			str = ((AccountComboboxEntry) acc).address;
			EventBus.getDefault().post(new AccountChangeEvent(nw, str));
		}
	}

	@SuppressWarnings("unchecked")
	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(NetworkChangeEvent e) {
		network_combobox.setModel(new ListComboBoxModel<>(MyDb.get_networks()));
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(SetNAABarEvent e) {
		if (!network_combobox.getSelectedItem().equals(e.getNetwork())) {
			network_combobox.setSelectedItem(e.getNetwork());
		}
		var adr = e.getAddress();
		for (var i = 0; i < 100; i++) {
			for (var j = 0; j < account_combobox.getModel().getSize(); j++) {
				var item = account_combobox.getModel().getElementAt(j).address;
				if (item.equals(adr)) {
					account_combobox.setSelectedIndex(j);
					return;
				}
			}
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException x) {
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountListUpdateEvent e) {
		update_account_combobox();
	}

}
