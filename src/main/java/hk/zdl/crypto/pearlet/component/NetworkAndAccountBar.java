package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.component.event.SettingsPanelEvent;
import hk.zdl.crypto.pearlet.ds.AddressWithNickname;
import hk.zdl.crypto.pearlet.ens.ENSLookup;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class NetworkAndAccountBar extends JPanel {

	private final JPanel left = new JPanel(new FlowLayout()), right = new JPanel(new FlowLayout());
	private final JComboBox<CrptoNetworks> network_combobox = new JComboBox<>();
	private final JComboBox<AddressWithNickname> account_combobox = new JComboBox<>();
	private List<Record> accounts = Arrays.asList();

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

		network_combobox.setModel(new EnumComboBoxModel<>(CrptoNetworks.class));
		network_combobox.addActionListener(e -> update_account_combobox());

		manage_network_btn.addActionListener(e -> EventBus.getDefault().post(new SettingsPanelEvent(SettingsPanelEvent.NET)));
		manage_account_btn.addActionListener(e -> EventBus.getDefault().post(new SettingsPanelEvent(SettingsPanelEvent.ACC)));

		account_combobox.addActionListener(e -> update_current_account());

	}

	@SuppressWarnings("unchecked")
	private final void update_account_combobox() {
		CrptoNetworks nw = (CrptoNetworks) network_combobox.getSelectedItem();
		List<AddressWithNickname> l = accounts.stream().filter(o -> o.getStr("NETWORK").equals(nw.name())).map(o -> o.getStr("ADDRESS")).map(o -> new AddressWithNickname(o))
				.collect(Collectors.toList());
		account_combobox.setModel(new ListComboBoxModel<>(l));
		account_combobox.setEnabled(!l.isEmpty());
		update_current_account();
		update_nickname(l);
	}

	private void update_current_account() {
		CrptoNetworks nw = (CrptoNetworks) network_combobox.getSelectedItem();
		String str = null;
		var acc = account_combobox.getSelectedItem();
		if (acc != null) {
			str = ((AddressWithNickname) acc).address;
		}
		EventBus.getDefault().post(new AccountChangeEvent(nw, str));
	}

	private void update_nickname(List<AddressWithNickname> l) {
		Util.submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				for (AddressWithNickname a : l) {
					String str = ENSLookup.reverse_lookup(a.address);
					if (str != null) {
						a.nickname = str;
						SwingUtilities.invokeLater(() -> account_combobox.updateUI());
					}
				}
				return null;
			}
		});
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountListUpdateEvent e) {
		accounts = e.getAccounts();
		update_account_combobox();
	}

}
