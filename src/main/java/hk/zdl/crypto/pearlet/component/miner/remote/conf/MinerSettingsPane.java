package hk.zdl.crypto.pearlet.component.miner.remote.conf;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.http.HttpClient;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;

public class MinerSettingsPane extends JTabbedPane implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3040618948690123951L;
	private final JPanel my_panel = new JPanel(new GridLayout(1, 2));
	private final MinerAccountSettingsPanel account_settings_panel = new MinerAccountSettingsPanel();
	private final MinerPathSettingPanel miner_path_settings_panel = new MinerPathSettingPanel();
	private final MinerServerAddressSettingsPane server_address_settings_panel = new MinerServerAddressSettingsPane();
	private final Timer timer = new Timer((int) TimeUnit.SECONDS.toMillis(10), this);

	public MinerSettingsPane() {
		init_panels();
	}

	private void init_panels() {
		my_panel.add(account_settings_panel);
		my_panel.add(miner_path_settings_panel);
		account_settings_panel.addListSelectionListener(e -> {
			@SuppressWarnings("unchecked")
			JList<String> list = (JList<String>) e.getSource();
			String selected = list.getSelectedValue();
			if (selected != null) {
				try {
					miner_path_settings_panel.setId(selected);
					miner_path_settings_panel.refresh_list();
				} catch (Exception x) {
					Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
				}
			}
		});
		my_panel.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent e) {
				timer.start();
				actionPerformed(null);
			}
		});
		addTab("Account and Directory", my_panel);
		addTab("Server URL", server_address_settings_panel);
	}

	public void setBasePath(String basePath) {
		account_settings_panel.setBasePath(basePath);
		miner_path_settings_panel.setBasePath(basePath);
		server_address_settings_panel.setBasePath(basePath);
	}

	public void setClient(HttpClient client) {
		account_settings_panel.setClient(client);
		miner_path_settings_panel.setClient(client);
		server_address_settings_panel.setClient(client);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			account_settings_panel.refresh_list();
		} catch (Exception x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
		}
		try {
			server_address_settings_panel.update_server_address();
		} catch (Exception x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
		}
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		timer.stop();
	}

}
