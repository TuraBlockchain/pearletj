package hk.zdl.crypto.pearlet.component.miner.remote.conf;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.http.HttpClient;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.miner.remote.ClientUpdateEvent;
import hk.zdl.crypto.pearlet.util.Util;

public class MinerSettingsPane extends JTabbedPane implements ActionListener {

	private static final long serialVersionUID = -3040618948690123951L;
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	private final JPanel my_panel = new JPanel(new GridLayout(1, 2));
	private final MinerAccountSettingsPanel account_settings_panel = new MinerAccountSettingsPanel();
	private final MinerPathSettingPanel miner_path_settings_panel = new MinerPathSettingPanel();
	private final MinerServerAddressSettingsPane server_address_settings_panel = new MinerServerAddressSettingsPane();
	private final AuthSchemePanel auth_scheme_panel = new AuthSchemePanel();
	private final Timer timer = new Timer((int) TimeUnit.SECONDS.toMillis(10), this);
	private String basePath = "";

	public MinerSettingsPane() {
		init_panels();
		EventBus.getDefault().register(this);
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
		addTab(rsc_bdl.getString("MINING.TAB.ACC_DIR"), my_panel);
		addTab(rsc_bdl.getString("MINING.TAB.SERVER"), server_address_settings_panel);
		addTab(rsc_bdl.getString("MINING.TAB.AUTH"), auth_scheme_panel);
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
		account_settings_panel.setBasePath(basePath);
		miner_path_settings_panel.setBasePath(basePath);
		server_address_settings_panel.setBasePath(basePath);
		auth_scheme_panel.setBasePath(basePath);
	}

	public void setClient(HttpClient client) {
		account_settings_panel.setClient(client);
		miner_path_settings_panel.setClient(client);
		server_address_settings_panel.setClient(client);
		auth_scheme_panel.setClient(client);
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

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(ClientUpdateEvent e) {
		if (basePath.equals(e.base_path)) {
			setClient(e.client);
		}
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		timer.stop();
		EventBus.getDefault().unregister(this);
	}

}
