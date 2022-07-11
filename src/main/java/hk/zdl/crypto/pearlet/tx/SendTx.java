package hk.zdl.crypto.pearlet.tx;

import static hk.zdl.crypto.pearlet.util.CrptoNetworks.ROTURA;
import static hk.zdl.crypto.pearlet.util.CrptoNetworks.SIGNUM;
import static hk.zdl.crypto.pearlet.util.CrptoNetworks.WEB3J;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;

public class SendTx implements Callable<Boolean> {

	private final CrptoNetworks network;
	private final String from, to;
	private final BigDecimal amount, fee;
	private boolean isEncrypted = false;
	private byte[] bin_message;
	private String str_message;
	private String asset_id;

	public SendTx(CrptoNetworks network, String from, String to, BigDecimal amount, BigDecimal fee, String asset_id) {
		this.network = network;
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.fee = fee;
		this.asset_id = asset_id;
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
		Optional<Record> o_r = MyDb.getAccount(network, from);
		if (o_r.isPresent()) {
			byte[] private_key = o_r.get().getBytes("PRIVATE_KEY");
			byte[] public_key = o_r.get().getBytes("PUBLIC_KEY");
			if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
				byte[] tx = new byte[0];
				if (asset_id == null) {
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
				} else {
					if (str_message != null && !str_message.isBlank()) {
						if (str_message.getBytes().length > 1000) {
							return false;
						} else {
							if (isEncrypted) {
//								tx = CryptoUtil.generateTransactionWithEncryptedMessage(network, to, public_key, amount, fee, str_message.getBytes(), true);
							} else {
								tx = CryptoUtil.generateTransferAssetTransactionWithMessage(network, public_key, to, asset_id, amount, fee,str_message);
							}
						}
					} else if (bin_message != null) {
						if (bin_message.length > 1000) {
							return false;
						} else {
							if (isEncrypted) {
//								tx = CryptoUtil.generateTransactionWithEncryptedMessage(network, to, public_key, amount, fee, bin_message, false);
							} else {
								tx = CryptoUtil.generateTransferAssetTransactionWithMessage(network, public_key, to, asset_id, amount, fee,bin_message);
							}
						}
					} else {
						tx = CryptoUtil.generateTransferAssetTransaction(network, public_key, to, asset_id, amount, fee);
					}
				}
				byte[] signed_tx = CryptoUtil.signTransaction(network, private_key, tx);

				Object obj = CryptoUtil.broadcastTransaction(network, signed_tx);
				return obj != null;
			} else if (WEB3J.equals(network)) {
				Optional<Web3j> o_j = CryptoUtil.getWeb3j();
				if (o_j.isPresent()) {
					Credentials credentials = Credentials.create(ECKeyPair.create(private_key));
					TransactionReceipt transactionReceipt = Transfer.sendFunds(o_j.get(), credentials, to, amount, Convert.Unit.ETHER).send();
					System.out.println(transactionReceipt);
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

}
