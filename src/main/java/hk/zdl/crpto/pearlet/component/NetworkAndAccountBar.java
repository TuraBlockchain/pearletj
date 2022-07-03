package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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

import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crpto.pearlet.MyToolbar;
import hk.zdl.crpto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crpto.pearlet.component.event.SettingsPanelEvent;
import hk.zdl.crpto.pearlet.util.CryptoUtil;

@SuppressWarnings("serial")
public class NetworkAndAccountBar extends JPanel {

	private final JPanel left = new JPanel(new FlowLayout()), right = new JPanel(new FlowLayout());
	private final JComboBox<String> network_combobox = new JComboBox<String>(), account_combobox = new JComboBox<String>();
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

		List<String> nws = Arrays.asList();
		try {
			nws = IOUtils.readLines(SettingsPanel.class.getClassLoader().getResourceAsStream("networks.txt"), "UTF-8");
		} catch (IOException e) {
		}
		network_combobox.setModel(new ListComboBoxModel<String>(nws));
		network_combobox.addActionListener(e -> update_account_combobox());

		manage_network_btn.addActionListener(e -> EventBus.getDefault().post(new SettingsPanelEvent(SettingsPanelEvent.NET)));
		manage_account_btn.addActionListener(e -> EventBus.getDefault().post(new SettingsPanelEvent(SettingsPanelEvent.ACC)));

	}

	@SuppressWarnings("unchecked")
	private final void update_account_combobox() {
		var nw = network_combobox.getSelectedItem().toString();
		List<String> l = accounts.stream().filter(o -> o.getStr("NETWORK").equals(nw)).map(o -> CryptoUtil.getAddress(nw, o.getBytes("PUBLIC_KEY"))).collect(Collectors.toList());
		account_combobox.setModel(new ListComboBoxModel<>(l));
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountListUpdateEvent e) {
		accounts = e.getAccounts();
		update_account_combobox();
	}

}
