package hk.zdl.crypto.pearlet.component.miner.local;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.crypto.SignumCrypto;
import signumj.entity.SignumAddress;

public class StartPanel extends JPanel {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private static final long serialVersionUID = 1278363752513931443L;
	private final JList<String> path_list = new JList<>(new DefaultListModel<String>());
	private final JButton run_btn = new JButton("Run");
	private CrptoNetworks network;
	private String account;

	public StartPanel(LocalMinerPanel pane) {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
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

		run_btn.addActionListener(e -> {
			var l_m = ((DefaultListModel<String>) path_list.getModel());
			if (l_m.isEmpty()) {
				return;
			}
			Icon icon = UIUtil.getStretchIcon("icon/" + "wallet_2.svg", 64, 64);
			String passphase = String.valueOf(JOptionPane.showInputDialog(getRootPane(), "Please input account passphase:", "Start Mining", JOptionPane.INFORMATION_MESSAGE, icon, null, null)).trim();
			if ("null".equals(String.valueOf(passphase)) || passphase.isBlank()) {
				JOptionPane.showMessageDialog(getRootPane(), "Passphase cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String id = SignumAddress.fromRs(account).getID();
			String _id = SignumCrypto.getInstance().getAddressFromPassphrase(passphase).getID();
			if (!id.equals(_id)) {
				JOptionPane.showMessageDialog(getRootPane(), "Passphase not match with account ID!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				var url = new URL(MyDb.get_server_url(network).get());
				var plot_dirs = Stream.of(l_m.toArray()).map(o -> Path.of(o.toString())).toList();
				var proc = LocalMiner.start(id, passphase, plot_dirs, url, null);
				var m_p = new MinerPanel(proc);
				m_p.setNetwork(network);
				pane.addTab(id, m_p);
				Util.submit(m_p);
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
				return;
			}
		});
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		this.network = e.network;
		this.account = e.account;
		var l_m = ((DefaultListModel<String>) path_list.getModel());
		l_m.clear();
		if ((Arrays.asList(CrptoNetworks.SIGNUM, CrptoNetworks.ROTURA).contains(network))) {
			var id = SignumAddress.fromRs(account.replace("TS-", "S-")).getID();
			MyDb.getMinerPaths(network, id).stream().map(o -> o.toAbsolutePath().toString()).forEach(l_m::addElement);
			run_btn.setEnabled(true);
		} else {
			run_btn.setEnabled(false);
		}
	}
}
