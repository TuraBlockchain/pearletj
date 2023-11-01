package hk.zdl.crypto.pearlet.tx;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.lock.CryptoAccount;
import hk.zdl.crypto.pearlet.util.CryptoUtil;

public class SendTx implements Callable<Boolean> {

	private final CryptoNetwork network;
	private final String from, to;
	private final BigDecimal amount, fee;
	private boolean isEncrypted = false;
	private byte[] bin_message;
	private String str_message;
	private String asset_id;

	public SendTx(CryptoNetwork network, String from, String to, BigDecimal amount, BigDecimal fee, String asset_id) {
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
		var  o_r = CryptoAccount.getAccount(network, from);
		if (o_r.isPresent()) {
			byte[] private_key = o_r.get().getPrivateKey();
			byte[] public_key = o_r.get().getPublicKey();
			if (network.isBurst()) {
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
								tx = CryptoUtil.generateTransferAssetTransactionWithEncryptedMessage(network, public_key, to, asset_id, amount, fee, str_message.getBytes(), true);
							} else {
								tx = CryptoUtil.generateTransferAssetTransactionWithMessage(network, public_key, to, asset_id, amount, fee, str_message);
							}
						}
					} else if (bin_message != null) {
						if (bin_message.length > 1000) {
							return false;
						} else {
							if (isEncrypted) {
								tx = CryptoUtil.generateTransferAssetTransactionWithEncryptedMessage(network, public_key, to, asset_id, amount, fee, bin_message, false);
							} else {
								tx = CryptoUtil.generateTransferAssetTransactionWithMessage(network, public_key, to, asset_id, amount, fee, bin_message);
							}
						}
					} else {
						tx = CryptoUtil.generateTransferAssetTransaction(network, public_key, to, asset_id, amount, fee);
					}
				}
				byte[] signed_tx = CryptoUtil.signTransaction(network, private_key, tx);

				Object obj = CryptoUtil.broadcastTransaction(network, signed_tx);
				return obj != null;
			} else if (network.isWeb3J()) {
				Optional<Web3j> o_j = CryptoUtil.getWeb3j();
				if (o_j.isPresent()) {
					Credentials credentials = Credentials.create(ECKeyPair.create(private_key));
					if (asset_id == null || "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee".equals(asset_id)) {// native ETH
						Transfer.sendFunds(o_j.get(), credentials, to, amount, Convert.Unit.WEI).send();
					} else {// ERC20
						ERC20.load(asset_id, o_j.get(), credentials, new DefaultGasProvider()).transfer(to, amount.toBigInteger()).send();
					}
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

}
