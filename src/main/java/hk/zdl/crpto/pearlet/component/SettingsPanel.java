package hk.zdl.crpto.pearlet.component;

import javax.swing.JTabbedPane;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crpto.pearlet.component.event.SettingsPanelEvent;

@SuppressWarnings("serial")
public class SettingsPanel extends JTabbedPane {

	public SettingsPanel() {
		addTab(SettingsPanelEvent.NET, new NetworkSettingsPanel());
		addTab(SettingsPanelEvent.ACC, new AccountSettingsPanel());
		EventBus.getDefault().register(this);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(SettingsPanelEvent e) {
		setSelectedIndex(indexOfTab(e.getString()));
	}

}
