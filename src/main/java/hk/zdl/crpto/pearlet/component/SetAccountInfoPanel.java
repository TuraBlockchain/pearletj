package hk.zdl.crpto.pearlet.component;

import javax.swing.JPanel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crpto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crpto.pearlet.util.CrptoNetworks;

@SuppressWarnings("serial")
public class SetAccountInfoPanel extends JPanel {
	private CrptoNetworks network;
	private String account;
	private byte[] public_key;

	public SetAccountInfoPanel() {
		EventBus.getDefault().register(this);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		this.network = e.network;
		this.account = e.account;
	}
}
