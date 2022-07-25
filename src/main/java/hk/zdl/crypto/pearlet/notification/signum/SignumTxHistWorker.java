package hk.zdl.crypto.pearlet.notification.signum;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;

public class SignumTxHistWorker extends Thread {

	private boolean running = true;
	private CrptoNetworks nw;
	private String address = "";
	private int ceil_block_height = Integer.MIN_VALUE;
	private final TreeSet<JSONObject> records = new TreeSet<JSONObject>(new SignumTxHistoryComparator().reversed());
	private final List<TxListener> listeners = new LinkedList<>();
	private long interval = TimeUnit.SECONDS.toMillis(10);

	public SignumTxHistWorker() {
		super(SignumTxHistWorker.class.getName());
		setDaemon(true);
		setPriority(MIN_PRIORITY);
	}

	@Override
	public void run() {
		while (running) {
			try {
				dig(0, true);
			} catch (SocketTimeoutException x) {
				continue;
			} catch (Exception x) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
			}
		}
		while (running) {
			try {
				dig(50, true);
			} catch (SocketTimeoutException x) {
				continue;
			} catch (Exception x) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
			}
			try {
				sleep(interval);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	public void dig(int depth, boolean first_dig) throws Exception {
		if (depth < 0) {
			return;
		}
		var jarr = CryptoUtil.getSignumTxID(nw, address, depth, depth + 1);
		if (jarr.length() < 1) {
			dig(depth - 1, false);
			return;
		}
		var jobj = jarr.getJSONObject(0);
		records.add(jobj);
		int this_block_height = jobj.getInt("block");
		if (this_block_height > ceil_block_height) {
			if (first_dig) {
				dig(depth * 2, true);
			} else {
				ceil_block_height = this_block_height;
				if (filter(jobj)) {
					listeners.stream().forEach(l -> l.transcationReceived(jobj));
				}
			}
		}
		dig(depth - 1, false);
	}

	protected boolean filter(JSONObject jobj) {
		return jobj.getString("to_address").equals(address);
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setCrptoNetworks(CrptoNetworks nw) {
		this.nw = nw;
	}

	public boolean add(TxListener e) {
		return listeners.add(e);
	}

	public boolean remove(TxListener o) {
		return listeners.remove(o);
	}

	public static interface TxListener {
		public void transcationReceived(JSONObject jobj);
	}

	public List<JSONObject> getRecords() {
		return Collections.unmodifiableList(new ArrayList<>(records));
	}
}
