package hk.zdl.crpto.pearlet.tx_history_query;

import org.greenrobot.eventbus.EventBus;

import hk.zdl.crpto.pearlet.component.event.TxHistoryEvent;
import hk.zdl.crpto.pearlet.util.CrptoNetworks;
import hk.zdl.crpto.pearlet.util.CryptoUtil;
import signumj.entity.SignumID;
import signumj.entity.response.Transaction;

public class SignumTxHistoryQuery implements TxHistoryQuery {

	@Override
	public void queryTxHistory(String address) throws Exception {
		SignumID[] tx_id_arr = CryptoUtil.getSignumTxID(address);
		for (SignumID id : tx_id_arr) {
			Transaction tx = CryptoUtil.getSignumTx(id);
			EventBus.getDefault().post(new TxHistoryEvent<Transaction>(CrptoNetworks.SIGNUM, TxHistoryEvent.Type.INSERT, tx));
		}

	}

}
