package hk.zdl.crpto.pearlet.component.dashboard;

import java.util.List;
import java.util.stream.Collectors;

import hk.zdl.crpto.pearlet.util.CrptoNetworks;
import hk.zdl.crpto.pearlet.util.CryptoUtil;
import signumj.entity.response.Transaction;

public class SignumTxUpdate implements DashboardTxUpdate {

	@Override
	public void do_update(DashboardTxTableModel table_model, String address) throws Exception {
		List<Transaction> list = CryptoUtil.getTranscations(CrptoNetworks.SIGNUM, address);
		List<Object[]> data = list.stream().map(o -> new Object[] { o.getId().getID(), o.getTimestamp().getAsInstant(), o.getType(), o.getAmount().toFormattedString(), o.getSender() })
				.collect(Collectors.toUnmodifiableList());
		table_model.setData(data);
	}

}
