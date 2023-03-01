package hk.zdl.crypto.pearlet.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.event.NetworkChangeEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.notification.TxListener;
import hk.zdl.crypto.pearlet.notification.ether.EtherAccountsMonitor;
import hk.zdl.crypto.pearlet.notification.signum.SignumAccountsMonitor;
import hk.zdl.crypto.pearlet.persistence.MyDb;

public class NWMon implements Callable<Void> {

	private final Map<CryptoNetwork, TxListener> map = new HashMap<>();

	@Override
	public Void call() throws Exception {
		EventBus.getDefault().register(this);
		for (var n : MyDb.get_networks()) {
			if (n.isBurst()) {
				var l = new SignumAccountsMonitor(n);
				map.put(n, l);
			} else if (n.isWeb3J()) {
				var l = new EtherAccountsMonitor(n);
				map.put(n, l);
			}
		}
		return null;
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(NetworkChangeEvent e) {
		var list = MyDb.get_networks();
		for (var n : list) {
			if (!map.containsKey(n)) {
				if (n.isBurst()) {
					var l = new SignumAccountsMonitor(n);
					map.put(n, l);
				} else if (n.isWeb3J()) {
					var l = new EtherAccountsMonitor(n);
					map.put(n, l);
				}
			}
		}
		var itr = map.keySet().iterator();
		while (itr.hasNext()) {
			var o = itr.next();
			if (!list.contains(o)) {
				itr.remove();
				EventBus.getDefault().unregister(o);
			}
		}
	}
}
