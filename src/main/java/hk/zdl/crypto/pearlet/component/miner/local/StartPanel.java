package hk.zdl.crypto.pearlet.component.miner.local;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
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
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.lock.WalletLock;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;
import hk.zdl.crypto.tura.miner.util.LocalMiner;
import signumj.crypto.SignumCrypto;
import signumj.entity.SignumAddress;

public class StartPanel extends JPanel {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private static final long serialVersionUID = 1278363752513931443L;
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	private final JList<String> path_list = new JList<>(new DefaultListModel<String>());
	private final JButton add_btn = new JButton(rsc_bdl.getString("MINER.LOCAL.ADD"));
	private final JButton del_btn = new JButton(rsc_bdl.getString("MINER.LOCAL.DEL"));
	private final JButton run_btn = new JButton(rsc_bdl.getString("MINER.LOCAL.RUN"));
	private LocalMinerPanel pane;
	private CryptoNetwork network;
	private String account;

	public StartPanel(LocalMinerPanel pane) {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
		this.pane = pane;
		JScrollPane scr = new JScrollPane(path_list);
		scr.setBorder(BorderFactory.createTitledBorder(rsc_bdl.getString("MINER.LOCAL.PATH.TEXT")));
		add(scr, BorderLayout.CENTER);

		var btn_panel = new JPanel(new GridBagLayout());
		btn_panel.add(add_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(del_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(run_btn, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

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
		var solo_mining = new JMenuItem(rsc_bdl.getString("MINER.LOCAL.RUN.SOLO"));
		var pool_mining = new JMenuItem(rsc_bdl.getString("MINER.LOCAL.RUN.POOL"));
		Stream.of(solo_mining, pool_mining).forEach(mining_menu::add);

		run_btn.addActionListener(e -> mining_menu.show(run_btn, 0, 0));
		solo_mining.addActionListener(e -> start_mining(true));
		pool_mining.addActionListener(e -> start_mining(false));
	}

	private void start_mining(boolean solo) {
		var l_m = ((DefaultListModel<String>) path_list.getModel());
		if (l_m.isEmpty()) {
			JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("MINER.LOCAL.ERR.MSG.PATH_UNA"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			String url = MyDb.get_server_url(network).get();
			String id = SignumAddress.fromRs(account).getID();
			String passphrase = null;
			if (solo) {
				int account_id = MyDb.getAccount(network, account).get().getInt("ID");
				if (WalletLock.isLocked() && WalletLock.unlock().orElseGet(() -> false) == true) {
					try {
						passphrase = WalletLock.decrypt_passphrase(network.getId(), account_id);
						var _id = SignumCrypto.getInstance().getAddressFromPassphrase(passphrase).getID();
						if (!id.equals(_id)) {
							passphrase = null;
						}
					} catch (Exception x) {
						passphrase = null;
					}
				}
				if (passphrase == null) {
					var icon = UIUtil.getStretchIcon("icon/wallet_2.svg", 64, 64);
					passphrase = (String) JOptionPane.showInputDialog(getRootPane(), rsc_bdl.getString("MINER.LOCAL.INPUT.PHRASE"), rsc_bdl.getString("MINER.LOCAL.START"),
							JOptionPane.INFORMATION_MESSAGE, icon, null, null);
					if (passphrase == null) {
						return;
					} else if (passphrase.isBlank()) {
						JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("MINER.LOCAL.ERR.MSG.PHRASE_EMPTY"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
						return;
					}
					passphrase = passphrase.trim();
					var _id = SignumCrypto.getInstance().getAddressFromPassphrase(passphrase).getID();
					if (!id.equals(_id)) {
						JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("MINER.LOCAL.ERR.MSG.PHRASE_NOT_MATCH"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				if (WalletLock.hasPassword() && (!WalletLock.isLocked() || WalletLock.unlock().orElseGet(() -> false) == true)) {
					var enc_pse = WalletLock.encrypt_private_key(Charset.defaultCharset().encode(passphrase).array());
					MyDb.insert_or_update_encpse(network.getId(), account_id, enc_pse);
				}
			} else {
				url = JOptionPane.showInputDialog(getRootPane(), rsc_bdl.getString("MINER.LOCAL.INPUT.POOL_URL"));
				if (url == null) {
					return;
				} else if (url.isBlank()) {
					JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("MINER.LOCAL.ERR.MSG.POOL_URL_EMPTY"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					new URL(url);
				}
				String _id = "";
				var opt = CryptoUtil.getRewardRecipient(network, account);
				if (opt.isPresent()) {
					_id = SignumAddress.fromEither(opt.get()).getID();
				}
				if (opt.isEmpty() || id.equals(_id)) {
					JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("MINER.LOCAL.ERR.MSG.REWARD_NOT_SET"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			var plot_dirs = Stream.of(l_m.toArray()).map(o -> Path.of(o.toString())).toList();
			var conf_file = LocalMiner.build_conf_file(id, passphrase, plot_dirs, new URL(url), null);
			var miner_bin = LocalMiner.copy_miner();
			var fnw = new ForgeNortiWorker(network, account);
			var m_p = new MinerPanel(miner_bin, conf_file) {

				private static final long serialVersionUID = 7669915543044846972L;

				@Override
				public void stop() {
					super.stop();
					fnw.stop();
				}
			};
			m_p.setNetwork(network);
			m_p.setPlotDirs(plot_dirs);
			pane.addTab(id, m_p);
			pane.setSelectedComponent(m_p);
			Util.submit(m_p);
			if (solo) {
				Util.submit(fnw);
			}
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
		var b = new boolean[] { false };
		if (account != null && network.isBurst()) {
			var id = SignumAddress.fromRs(account.replace("TS-", "S-")).getID();
			MyDb.getMinerPaths(network, id).stream().map(o -> o.toAbsolutePath().toString()).forEach(l_m::addElement);
			b[0] = true;
		}
		Stream.of(add_btn, del_btn, run_btn).forEach(o -> o.setEnabled(b[0]));
	}
}
