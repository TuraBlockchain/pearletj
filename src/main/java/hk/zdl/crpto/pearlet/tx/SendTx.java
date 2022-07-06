package hk.zdl.crpto.pearlet.tx;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import hk.zdl.crpto.pearlet.util.CrptoNetworks;

public class SendTx implements Callable<Boolean> {
	
	private final CrptoNetworks network;
	private final String from,to;
	private final BigDecimal amount,fee;

	public SendTx(CrptoNetworks network, String from, String to, BigDecimal amount, BigDecimal fee) {
		super();
		this.network = network;
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.fee = fee;
	}

	@Override
	public Boolean call() throws Exception {
		return false;
	}

}
