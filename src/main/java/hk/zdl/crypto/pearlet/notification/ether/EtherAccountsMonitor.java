package hk.zdl.crypto.pearlet.notification.ether;

import java.awt.TrayIcon.MessageType;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.notification.TxListener;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;

public class EtherAccountsMonitor implements TxListener {

	private final Map<String, EtherTxHistWorker> map = new TreeMap<>();
	private final CryptoNetwork nw;

	public EtherAccountsMonitor(CryptoNetwork nw) {
		this.nw = nw;
		EventBus.getDefault().register(this);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountListUpdateEvent e) {
		List<String> l = MyDb.getAccounts().stream().filter(o -> o.getInt("NWID") == nw.getId()).map(o -> o.getStr("ADDRESS")).toList();
		process_add_worker(l);
		process_del_worker(l);
	}

	private void process_add_worker(List<String> l) {
		for (var s : l) {
			if (!map.containsKey(s)) {
				var worker = new EtherTxHistWorker();
				worker.setAddress(s);
				worker.setRunning(true);
				map.put(s, worker);
				worker.add(this);
				worker.start();
			}
		}

	}

	@SuppressWarnings({ "removal" })
	private void process_del_worker(List<String> l) {
		var x = new LinkedList<String>();
		for (var s : map.keySet()) {
			if (!l.contains(s)) {
				x.add(s);
			}
		}
		for (var s : x) {
			var worker = map.remove(s);
			if (worker != null) {
				worker.setRunning(false);
				try {
					worker.interrupt();
				} catch (Exception e) {
					continue;
				}
				try {
					worker.stop();
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public void transcationReceived(JSONObject jobj) {
		UIUtil.displayMessage("", "You have received tokens!", MessageType.INFO);
	}
}
