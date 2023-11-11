package hk.zdl.crypto.pearlet.component.settings;

import java.util.ResourceBundle;

import javax.swing.JTabbedPane;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.event.SettingsPanelEvent;
import hk.zdl.crypto.pearlet.util.Util;

public class SettingsPanel extends JTabbedPane {

	private static final long serialVersionUID = 7411505537520396171L;
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();

	public SettingsPanel() {
		addTab(rsc_bdl.getString("SETTINGS.NETWORK.TAB_NAME"), new NetworkSettingsPanel());
		addTab(rsc_bdl.getString("SETTINGS.ACCOUNT.TAB_NAME"), new AccountSettingsPanel());
		addTab(rsc_bdl.getString("SETTINGS.DISPLAY.TAB_NAME"), new DisplaySettingsPanel());
		addTab(rsc_bdl.getString("SETTINGS.LOCK.TAB_NAME"), new LockWalletPanel());
		EventBus.getDefault().register(this);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(SettingsPanelEvent e) {
		setSelectedIndex(e.getIndex());
	}

}
