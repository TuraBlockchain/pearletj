package hk.zdl.crpto.pearlet.tx;

import static hk.zdl.crpto.pearlet.util.CrptoNetworks.ROTURA;
import static hk.zdl.crpto.pearlet.util.CrptoNetworks.SIGNUM;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.Callable;

import hk.zdl.crpto.pearlet.persistence.MyDb;
import hk.zdl.crpto.pearlet.util.CrptoNetworks;
import hk.zdl.crpto.pearlet.util.CryptoUtil;
import signumj.crypto.SignumCrypto;

public class SendTx implements Callable<Boolean> {

	private final CrptoNetworks network;
	private final String from, to;
	private final BigDecimal amount, fee;
	private boolean isEncrypted = false;
	private byte[] bin_message;
	private String str_message;

	public SendTx(CrptoNetworks network, String from, String to, BigDecimal amount, BigDecimal fee) {
		super();
		this.network = network;
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.fee = fee;
	}

	public void setEncrypted(boolean b) {
		isEncrypted = b;
	}

	public void setMessage(byte[] b) {
		bin_message = b;
	}

	public void setMessage(String str) {
		str_message = str;
	}

	@Override
	public Boolean call() throws Exception {
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			byte[] private_key = null;
			for (var r : MyDb.getAccounts(network)) {
				byte[] _key = r.getBytes("PRIVATE_KEY");
				String raw_adr = SignumCrypto.getInstance().getAddressFromPrivate(_key).getRawAddress();
				if (from.endsWith(raw_adr)) {
					private_key = _key;
					break;
				}
			}
			if (private_key == null) {
				return false;
			}
			byte[] public_key = CryptoUtil.getPublicKey(network, private_key);
			byte[] tx;
			if (str_message != null && !str_message.isBlank()) {
				if (str_message.getBytes().length > 1000) {
					return false;
				} else {
					if (isEncrypted) {
						tx = CryptoUtil.generateTransactionWithEncryptedMessage(network, to, public_key, amount, fee, str_message.getBytes(), true);
					} else {
						tx = CryptoUtil.generateTransactionWithMessage(network, to, public_key, amount, fee, str_message);
					}
				}
			} else if (bin_message != null) {
				if (bin_message.length > 1000) {
					return false;
				} else {
					if (isEncrypted) {
						tx = CryptoUtil.generateTransactionWithEncryptedMessage(network, to, public_key, amount, fee, bin_message, false);
					} else {
						tx = CryptoUtil.generateTransactionWithMessage(network, to, public_key, amount, fee, bin_message);
					}
				}
			} else {
				tx = CryptoUtil.generateTransaction(network, to, public_key, amount, fee);
			}
			byte[] signed_tx = CryptoUtil.signTransaction(network, private_key, tx);

			Object obj = CryptoUtil.broadcastTransaction(network, signed_tx);
			return obj != null;
		}
		return true;
	}

}
