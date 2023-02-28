package hk.zdl.crypto.pearlet.tx_history_query;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import hk.zdl.crypto.pearlet.component.event.TxHistoryEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.util.CryptoUtil;

public class EtherTxQuery {

	private final CryptoNetwork nw;

	public EtherTxQuery(CryptoNetwork nw) {
		this.nw = nw;
	}

	public void queryTxHistory(String address) throws Exception {
		if (address == null) {
			return;
		}
		var items = CryptoUtil.getTxHistory(address, 0, Integer.MAX_VALUE);
		for (int i = 0; i < items.length(); i++) {
			var jobj = items.getJSONObject(i);
			EventBus.getDefault().post(new TxHistoryEvent<JSONObject>(nw, TxHistoryEvent.Type.INSERT, jobj));
		}

	}
}
