package hk.zdl.crypto.pearlet.component;

import static hk.zdl.crypto.pearlet.util.CrptoNetworks.ROTURA;
import static hk.zdl.crypto.pearlet.util.CrptoNetworks.SIGNUM;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.component.event.SettingsPanelEvent;
import hk.zdl.crypto.pearlet.component.event.TxHistoryEvent;
import hk.zdl.crypto.pearlet.ds.AccountComboboxEntry;
import hk.zdl.crypto.pearlet.ens.ENSLookup;
import hk.zdl.crypto.pearlet.ui.AccountComboboxRenderer;
import hk.zdl.crypto.pearlet.ui.MyListComboBoxModel;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.response.Transaction;
import signumj.response.attachment.AccountInfoAttachment;

@SuppressWarnings("serial")
public class NetworkAndAccountBar extends JPanel {

	private final JPanel left = new JPanel(new FlowLayout()), right = new JPanel(new FlowLayout());
	private final JComboBox<CrptoNetworks> network_combobox = new JComboBox<>();
	private final JComboBox<AccountComboboxEntry> account_combobox = new JComboBox<>();
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

		if (Util.getProp().getBoolean("show_peth_only")) {
			network_combobox.setModel(new ListComboBoxModel<>(Arrays.asList(CrptoNetworks.ROTURA)));
		} else {
			network_combobox.setModel(new EnumComboBoxModel<>(CrptoNetworks.class));
		}
		network_combobox.addActionListener(e -> update_account_combobox());

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
		CrptoNetworks nw = (CrptoNetworks) network_combobox.getSelectedItem();
		List<AccountComboboxEntry> l = accounts.stream().filter(o -> o.getStr("NETWORK").equals(nw.name())).map(o -> o.getStr("ADDRESS")).map(o -> new AccountComboboxEntry(nw, o, null)).toList();
		l = l.stream().map(o -> new AccountComboboxEntry(nw, o.address, ENSLookup.containsKey(o.address) ? ENSLookup.reverse_lookup(o.address) : null)).toList();
		account_combobox.setModel(new MyListComboBoxModel<>(new ArrayList<>(l)));
		account_combobox.setEnabled(!l.isEmpty());
	}

	private void update_current_account() {
		CrptoNetworks nw = (CrptoNetworks) network_combobox.getSelectedItem();
		String str = null;
		var acc = account_combobox.getSelectedItem();
		if (acc == null) {
			EventBus.getDefault().post(new AccountChangeEvent(nw, null));
		}else {
			str = ((AccountComboboxEntry) acc).address;
			EventBus.getDefault().post(new AccountChangeEvent(nw, str));
		}
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountListUpdateEvent e) {
		accounts = e.getAccounts();
		update_account_combobox();
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(TxHistoryEvent<?> e) {
		if (e.type.equals(TxHistoryEvent.Type.INSERT)) {
			if (Arrays.asList(SIGNUM, ROTURA).contains(e.network)) {
				Transaction tx = (Transaction) e.data;
				if (tx.getType() == 1 && tx.getSubtype() == 5) {
					if (e.network.equals(network_combobox.getSelectedItem())) {
						@SuppressWarnings("unchecked")
						MyListComboBoxModel<AccountComboboxEntry> model = (MyListComboBoxModel<AccountComboboxEntry>) account_combobox.getModel();
						for (int i = 0; i < model.getSize(); i++) {
							var a = model.getElementAt(i);
							var adr = a.address;
							adr = adr.substring(adr.indexOf('-') + 1);
							var address = tx.getSender().getRawAddress();
							if (adr.equals(address)) {
								var aliases = ((AccountInfoAttachment) tx.getAttachment()).getName();
								a.nickname = aliases;
								model.setElementAt(i, a);
								ENSLookup.put(a.address, aliases);
							}
						}
					}
				}
			}
		}
	}

}
