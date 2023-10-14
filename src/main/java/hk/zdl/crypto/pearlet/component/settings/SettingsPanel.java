package hk.zdl.crypto.pearlet.component.settings;

import javax.swing.JTabbedPane;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.event.SettingsPanelEvent;

@SuppressWarnings("serial")
public class SettingsPanel extends JTabbedPane {

	public SettingsPanel() {
		addTab(SettingsPanelEvent.NET, new NetworkSettingsPanel());
		addTab(SettingsPanelEvent.ACC, new AccountSettingsPanel());
		addTab("Display", new DisplaySettingsPanel());
		addTab("Lock", new LockWalletPanel());
		EventBus.getDefault().register(this);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(SettingsPanelEvent e) {
		setSelectedIndex(indexOfTab(e.getString()));
	}

}
