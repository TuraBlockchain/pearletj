package hk.zdl.crpto.pearlet.component.dashboard.signum;

import java.util.List;
import java.util.stream.Collectors;

import hk.zdl.crpto.pearlet.component.dashboard.DashboardTxTableModel;
import hk.zdl.crpto.pearlet.component.dashboard.DashboardTxUpdate;
import hk.zdl.crpto.pearlet.util.CrptoNetworks;
import hk.zdl.crpto.pearlet.util.CryptoUtil;
import signumj.entity.SignumAddress;
import signumj.entity.response.Transaction;

public class SignumTxUpdate implements DashboardTxUpdate {

	@Override
	public void do_update(DashboardTxTableModel table_model, String address) throws Exception {
		List<Transaction> list = CryptoUtil.getTranscations(CrptoNetworks.SIGNUM, address);
		List<Object[]> data = list.stream().map(o -> new Object[] { o.getId(), o.getTimestamp().getAsInstant(), o.getType(), o.getAmount(), new SignumAddress[] {o.getSender(),o.getRecipient()} })
				.collect(Collectors.toUnmodifiableList());
		table_model.setData(data);
	}

}
