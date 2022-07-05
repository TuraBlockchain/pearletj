package hk.zdl.crpto.pearlet.component.dashboard.signum;

import hk.zdl.crpto.pearlet.component.dashboard.DashboardTxTableModel;
import hk.zdl.crpto.pearlet.component.dashboard.DashboardTxUpdate;
import hk.zdl.crpto.pearlet.util.CryptoUtil;
import signumj.entity.SignumID;
import signumj.entity.response.Transaction;

public class SignumTxUpdate implements DashboardTxUpdate {

	@Override
	public void do_update(DashboardTxTableModel table_model, String address) throws Exception {
		SignumID[] tx_id_arr = CryptoUtil.getSignumTxID(address);
		for (SignumID id : tx_id_arr) {
			Transaction tx = CryptoUtil.getSignumTx(id);
			table_model.insertData(tx);
		}
	}

}
