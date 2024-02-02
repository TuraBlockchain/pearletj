package hk.zdl.crypto.pearlet.tx;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.lock.CryptoAccount;
import hk.zdl.crypto.pearlet.util.CryptoUtil;

public class MsgTx implements Callable<Boolean> {

	private final CryptoNetwork network;
	private final String from, to;
	private final BigDecimal fee;
	private String str_message;

	public MsgTx(CryptoNetwork network, String from, String to, BigDecimal fee) {
		this.network = network;
		this.from = from;
		this.to = to;
		this.fee = fee;
	}

	public void setMessage(String msg) {
		str_message = msg;
	}

	@Override
	public Boolean call() throws Exception {
		var o_r = CryptoAccount.getAccount(network, from);
		if (o_r.isPresent()) {
			byte[] public_key = o_r.get().getPublicKey();
			byte[] private_key = o_r.get().getPrivateKey();
			var tx = CryptoUtil.sendMessage(network, to, public_key, str_message,fee);
			byte[] signed_tx = CryptoUtil.signTransaction(network, private_key, tx);

			Object obj = CryptoUtil.broadcastTransaction(network, signed_tx);
			return obj != null;
		} else {
			return false;
		}
	}

}
