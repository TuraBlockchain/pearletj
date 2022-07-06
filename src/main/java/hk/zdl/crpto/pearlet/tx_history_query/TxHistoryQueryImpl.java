package hk.zdl.crpto.pearlet.tx_history_query;

import org.greenrobot.eventbus.EventBus;

import hk.zdl.crpto.pearlet.component.event.TxHistoryEvent;
import hk.zdl.crpto.pearlet.util.CrptoNetworks;
import hk.zdl.crpto.pearlet.util.CryptoUtil;
import signumj.entity.SignumID;
import signumj.entity.response.Transaction;

public class TxHistoryQueryImpl implements TxHistoryQuery {
	
	private final CrptoNetworks nw;

	public TxHistoryQueryImpl(CrptoNetworks nw) {
		this.nw = nw;
	}

	@Override
	public void queryTxHistory(String address) throws Exception {
		SignumID[] tx_id_arr = CryptoUtil.getSignumTxID(nw,address);
		for (SignumID id : tx_id_arr) {
			Transaction tx = CryptoUtil.getSignumTx(nw,id);
			EventBus.getDefault().post(new TxHistoryEvent<Transaction>(nw, TxHistoryEvent.Type.INSERT, tx));
		}

	}

}
