package hk.zdl.crypto.pearlet.tx_history_query;

import org.greenrobot.eventbus.EventBus;

import hk.zdl.crypto.pearlet.component.event.TxHistoryEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import signumj.entity.SignumID;
import signumj.entity.response.Transaction;

public class SignumTxHistoryQuery {

	private final CryptoNetwork nw;

	public SignumTxHistoryQuery(CryptoNetwork nw) {
		this.nw = nw;
	}

	public void queryTxHistory(String address) throws Exception {
		SignumID[] tx_id_arr = CryptoUtil.getSignumTxID(nw, address);
		for (SignumID id : tx_id_arr) {
			try {
				Transaction tx = CryptoUtil.getSignumTx(nw, id);
				EventBus.getDefault().post(new TxHistoryEvent<Transaction>(nw, TxHistoryEvent.Type.INSERT, tx));
			} catch (ThreadDeath e) {
				continue;
			}
		}

	}

}
