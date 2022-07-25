package hk.zdl.crypto.pearlet.notification.signum;

import java.awt.TrayIcon.MessageType;
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
			if (!map.containsKey(s)) {
				var worker = new SignumTxHistWorker();
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
		for (var s : l) {
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
