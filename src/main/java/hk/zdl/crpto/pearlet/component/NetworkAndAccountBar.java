package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
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

import hk.zdl.crpto.pearlet.MyToolbar;
import hk.zdl.crpto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crpto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crpto.pearlet.component.event.SettingsPanelEvent;
import hk.zdl.crpto.pearlet.util.CrptoNetworks;
import hk.zdl.crpto.pearlet.util.CryptoUtil;

@SuppressWarnings("serial")
public class NetworkAndAccountBar extends JPanel {

	private final JPanel left = new JPanel(new FlowLayout()), right = new JPanel(new FlowLayout());
	private final JComboBox<CrptoNetworks> network_combobox = new JComboBox<>();
	private final JComboBox<String> account_combobox = new JComboBox<>();
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
		network_combobox.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
		account_combobox.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));

		Icon btn_icon = null;
		try {
			btn_icon = new MyStretchIcon(ImageIO.read(MyToolbar.class.getClassLoader().getResource("toolbar/" + "screwdriver-wrench-solid.svg")), 24, 24);
		} catch (IOException e) {
		}
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
		List<String> l = accounts.stream().filter(o -> o.getStr("NETWORK").equals(nw.name())).map(o -> CryptoUtil.getAddress(nw, o.getBytes("PUBLIC_KEY"))).collect(Collectors.toList());
		account_combobox.setModel(new ListComboBoxModel<>(l));
		account_combobox.setEnabled(!l.isEmpty());
		update_current_account();
	}

	private void update_current_account() {
		CrptoNetworks nw = (CrptoNetworks) network_combobox.getSelectedItem();
		var acc = String.valueOf(account_combobox.getSelectedItem());
		EventBus.getDefault().post(new AccountChangeEvent(nw, acc));
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountListUpdateEvent e) {
		accounts = e.getAccounts();
		update_account_combobox();
	}

}
