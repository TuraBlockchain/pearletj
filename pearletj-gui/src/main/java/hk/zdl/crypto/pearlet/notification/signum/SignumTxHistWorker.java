package hk.zdl.crypto.pearlet.notification.signum;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
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
	private long blockTimestamp = Long.MIN_VALUE;
	private final List<TxListener> listeners = new LinkedList<>();

	public SignumTxHistWorker() {
		super(SignumTxHistWorker.class.getName());
		setDaemon(true);
		setPriority(MIN_PRIORITY);
	}

	@Override
	public void run() {
		while (running && blockTimestamp < 0) {
			try {
				dig(0, true);
			} catch (java.net.UnknownHostException x) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					return;
				}
				continue;
			} catch (SocketTimeoutException x) {
				continue;
			} catch (IOException x) {
				if (x.getMessage().equals("Unknown account")) {
					return;
				}
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
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	public void dig(int depth, boolean first_dig) throws Exception {
		if (depth < 0) {
			return;
		}
		var jarr = CryptoUtil.getSignumTxID(nw, address, depth, depth);
		if (jarr.length() < 1) {
			dig(depth - 1, false);
			return;
		}
		for (int i = jarr.length() - 1; i > -1; i--) {
			var tx_id = jarr.get(i).toString();
			var jobj = CryptoUtil.getSignumTx(nw, tx_id);
			long this_block_timestamp = jobj.getLong("blockTimestamp");
			if (blockTimestamp < 0) {
				blockTimestamp = this_block_timestamp;
				continue;
			} else if (this_block_timestamp > blockTimestamp) {
				if (first_dig) {
					dig(depth * 2, true);
				} else {
					blockTimestamp = this_block_timestamp;
					if (filter(jobj)) {
						listeners.stream().forEach(l -> l.transcationReceived(jobj));
					}
				}
			}
		}
		dig(depth - 1, false);
	}

	protected boolean filter(JSONObject jobj) {
		return jobj.optString("recipientRS","").equals(address);
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

}
