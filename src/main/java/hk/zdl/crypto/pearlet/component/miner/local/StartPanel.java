package hk.zdl.crypto.pearlet.component.miner.local;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.crypto.SignumCrypto;
import signumj.entity.SignumAddress;

public class StartPanel extends JPanel {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private static final long serialVersionUID = 1278363752513931443L;
	private final JList<String> path_list = new JList<>(new DefaultListModel<String>());
	private final JButton run_btn = new JButton("Run");
	private LocalMinerPanel pane;
	private CrptoNetworks network;
	private String account;

	public StartPanel(LocalMinerPanel pane) {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
		this.pane = pane;
		JScrollPane scr = new JScrollPane(path_list);
		scr.setBorder(BorderFactory.createTitledBorder("Miner Paths"));
		add(scr, BorderLayout.CENTER);

		var btn_panel = new JPanel(new GridBagLayout());
		var add_btn = new JButton("Add");
		btn_panel.add(add_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var del_btn = new JButton("Delete");
		btn_panel.add(del_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		btn_panel.add(run_btn, new GridBagConstraints(0, 2, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));

		var panel_1 = new JPanel(new FlowLayout(1, 0, 0));
		panel_1.add(btn_panel);
		add(panel_1, BorderLayout.EAST);

		add_btn.addActionListener((e) -> {
			var file_dialog = new JFileChooser();
			file_dialog.setDialogType(JFileChooser.OPEN_DIALOG);
			file_dialog.setMultiSelectionEnabled(false);
			file_dialog.setDragEnabled(false);
			file_dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int i = file_dialog.showOpenDialog(getRootPane());
			if (i != JFileChooser.APPROVE_OPTION) {
				return;
			}
			var str = file_dialog.getSelectedFile().getAbsolutePath();
			var id = SignumAddress.fromRs(account).getID();
			if (MyDb.addMinerPath(network, id, Paths.get(str))) {
				((DefaultListModel<String>) path_list.getModel()).addElement(str);
			}
		});

		del_btn.addActionListener(e -> {
			var model = (DefaultListModel<String>) path_list.getModel();
			int i = path_list.getSelectedIndex();
			if (i > -1) {
				var str = model.get(i);
				var id = SignumAddress.fromRs(account).getID();
				if (MyDb.delMinerPath(network, id, Paths.get(str))) {
					model.remove(i);
				}
			}
		});

		var mining_menu = new JPopupMenu();
		var solo_mining = new JMenuItem("Solo...");
		var pool_mining = new JMenuItem("in Pool");
		Stream.of(solo_mining, pool_mining).forEach(mining_menu::add);

		run_btn.addActionListener(e -> mining_menu.show(run_btn, 0, 0));
		solo_mining.addActionListener(e -> start_mining(true));
		pool_mining.addActionListener(e -> start_mining(false));
	}

	private void start_mining(boolean solo) {
		var l_m = ((DefaultListModel<String>) path_list.getModel());
		if (l_m.isEmpty()) {
			return;
		}
		try {
			String url = MyDb.get_server_url(network).get();
			String id = SignumAddress.fromRs(account).getID();
			String passphrase = null;
			if (solo) {
				var icon = UIUtil.getStretchIcon("icon/" + "wallet_2.svg", 64, 64);
				passphrase = String.valueOf(JOptionPane.showInputDialog(getRootPane(), "Please input account passphrase:", "Start Mining", JOptionPane.INFORMATION_MESSAGE, icon, null, null)).trim();
				if ("null".equals(String.valueOf(passphrase))) {
					return;
				} else if (passphrase.isBlank()) {
					JOptionPane.showMessageDialog(getRootPane(), "Passphrase cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
					return;

				}
				var _id = SignumCrypto.getInstance().getAddressFromPassphrase(passphrase).getID();
				if (!id.equals(_id)) {
					JOptionPane.showMessageDialog(getRootPane(), "Passphrase not match with account ID!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				url = String.valueOf(JOptionPane.showInputDialog(getRootPane(), "Please input URL of pool:")).trim();
				if ("null".equals(String.valueOf(url))) {
					return;
				} else if (url.isBlank()) {
					JOptionPane.showMessageDialog(getRootPane(), "Pool URL cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					new URL(url);
				}
				String _id = "";
				Optional<String> opt = CryptoUtil.getRewardRecipient(network, account);
				if (opt.isPresent()) {
					_id = SignumAddress.fromEither(opt.get()).getID();
				}
				if (opt.isEmpty() || id.equals(_id)) {
					JOptionPane.showMessageDialog(getRootPane(), "Reward recipient was not set for this account!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			var plot_dirs = Stream.of(l_m.toArray()).map(o -> Path.of(o.toString())).toList();
			var conf_file = LocalMiner.build_conf_file(id, passphrase, plot_dirs, new URL(url), null);
			var miner_bin = LocalMiner.copy_miner();
			var m_p = new MinerPanel(miner_bin, conf_file);
			m_p.setNetwork(network);
			m_p.setPlotDirs(plot_dirs);
			pane.addTab(id, m_p);
			Util.submit(m_p);
		} catch (Exception x) {
			JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		this.network = e.network;
		this.account = e.account;
		var l_m = ((DefaultListModel<String>) path_list.getModel());
		l_m.clear();
		if (account != null && (Arrays.asList(CrptoNetworks.SIGNUM, CrptoNetworks.ROTURA).contains(network))) {
			var id = SignumAddress.fromRs(account.replace("TS-", "S-")).getID();
			MyDb.getMinerPaths(network, id).stream().map(o -> o.toAbsolutePath().toString()).forEach(l_m::addElement);
			run_btn.setEnabled(true);
		} else {
			run_btn.setEnabled(false);
		}
	}
}
