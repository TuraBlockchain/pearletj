package hk.zdl.crypto.pearlet.notification.signum;

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
import hk.zdl.crypto.pearlet.notification.signum.SignumTxHistWorker.TxListener;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;

public class SignumAccountsMonitor implements TxListener {

	private final Map<String, SignumTxHistWorker> map = new TreeMap<>();
	private final CrptoNetworks nw;

	public SignumAccountsMonitor(CrptoNetworks nw) {
		this.nw = nw;
		EventBus.getDefault().register(this);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountListUpdateEvent e) {
		List<String> l = e.getAccounts().stream().filter(o -> o.getStr("NETWORK").equals(nw.name())).map(o -> o.getStr("ADDRESS")).toList();
		process_add_worker(l);
		process_del_worker(l);
	}

	private void process_add_worker(List<String> l) {
		for (var s : l) {
			if (s == null) {
				continue;
			}
			if (!map.containsKey(s)) {
				var worker = new SignumTxHistWorker();
				worker.setCrptoNetworks(nw);
				worker.setAddress(s);
				worker.setRunning(true);
				map.put(s, worker);
				worker.add(this);
				worker.start();
			}
		}

	}

	@SuppressWarnings("deprecation")
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
		var recipientRS = jobj.optString("recipientRS");
		if (map.containsKey(recipientRS)) {
			int tx_type = jobj.getInt("type");
			int sub_type = jobj.getInt("subtype");
			if (tx_type == 0 || (tx_type == 2 && sub_type == 1)) {
				UIUtil.displayMessage("", "You have received tokens!", MessageType.INFO);
			} else if (tx_type == 1 && sub_type == 0) {
				UIUtil.displayMessage("", "You got a message!", MessageType.INFO);
			}

		}
	}

}
