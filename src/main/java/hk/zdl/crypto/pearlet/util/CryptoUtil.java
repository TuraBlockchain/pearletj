package hk.zdl.crypto.pearlet.util;

import static hk.zdl.crypto.pearlet.util.CrptoNetworks.ROTURA;
import static hk.zdl.crypto.pearlet.util.CrptoNetworks.SIGNUM;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import hk.zdl.crypto.pearlet.ds.RoturaAddress;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import signumj.crypto.SignumCrypto;
import signumj.entity.EncryptedMessage;
import signumj.entity.SignumAddress;
import signumj.entity.SignumID;
import signumj.entity.SignumValue;
import signumj.entity.response.Transaction;
import signumj.entity.response.http.BRSError;
import signumj.service.NodeService;

public class CryptoUtil {
	

	public static final boolean isValidAddress(CrptoNetworks network, String address) {
		if (address == null || address.isBlank()) {
			return false;
		}
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			try {
				String adr = SignumAddress.fromRs(address).getRawAddress();
				String bdr = address.substring(address.indexOf('-') + 1);
				return adr.equals(bdr);
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	public static final byte[] getPublicKeyFromAddress(CrptoNetworks network, String addr) {
		if (network.equals(SIGNUM)) {
			return SignumAddress.fromRs(addr).getPublicKey();
		}
		throw new UnsupportedOperationException();
	}

	public static final byte[] getPublicKey(CrptoNetworks network, String type, String text) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			if (type.equalsIgnoreCase("phrase")) {
				return SignumCrypto.getInstance().getPublicKey(text);
			}
		}
		throw new UnsupportedOperationException();
	}

	public static final byte[] getPrivateKey(CrptoNetworks network, String type, String text) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			if (type.equalsIgnoreCase("phrase")) {
				return SignumCrypto.getInstance().getPrivateKey(text);
			} else if (type.equalsIgnoreCase("base64")) {
				return Base64.decode(text);
			} else if (type.equals("HEX")) {
				return Hex.decode(text);
			}
		}
		throw new UnsupportedOperationException();
	}

	public static final byte[] getPublicKey(CrptoNetworks network, byte[] private_key) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			return SignumCrypto.getInstance().getPublicKey(private_key);
		}
		throw new UnsupportedOperationException();
	}

	public static final String getAddress(CrptoNetworks network, byte[] public_key) {
		if (network.equals(SIGNUM)) {
			return SignumCrypto.getInstance().getAddressFromPublic(public_key).getFullAddress();
		} else if (network.equals(ROTURA)) {
			return new RoturaAddress(public_key).toString();
		}
		return null;
	}

	public static final BigDecimal getBalance(CrptoNetworks network, String address) throws Exception {
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			Optional<String> opt = get_server_url(network);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				try {
					return ns.getAccount(SignumAddress.fromRs(address)).toFuture().get().getBalance().toSigna();
				} catch (IllegalArgumentException | InterruptedException | ExecutionException e) {
					if (e.getCause() != null) {
						if (e.getCause().getClass().getName().equals("signumj.entity.response.http.BRSError")) {
							BRSError e1 = (BRSError) e.getCause();
							if (e1.getCode() == 5) {
								return new BigDecimal(0);
							}
						}
					}
					throw e;
				}
			}
			;
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] generateTransaction(CrptoNetworks nw, String recipient, byte[] public_key, BigDecimal amount, BigDecimal fee) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				return ns.generateTransaction(SignumAddress.fromRs(recipient), public_key, SignumValue.fromSigna(amount), SignumValue.fromSigna(fee), 1440, null).blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] generateTransactionWithEncryptedMessage(CrptoNetworks nw, String recipient, byte[] public_key, BigDecimal amount, BigDecimal fee, byte[] message, boolean isText) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				byte[] nounce = new byte[32];// must be 32
				new Random().nextBytes(nounce);
				EncryptedMessage emsg = new EncryptedMessage(message, nounce, isText);
				return ns.generateTransactionWithEncryptedMessage(SignumAddress.fromRs(recipient), public_key, SignumValue.fromSigna(amount), SignumValue.fromSigna(fee), 1440, emsg, null)
						.blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] generateTransactionWithMessage(CrptoNetworks nw, String recipient, byte[] public_key, BigDecimal amount, BigDecimal fee, String message) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				return ns.generateTransactionWithMessage(SignumAddress.fromRs(recipient), public_key, SignumValue.fromSigna(amount), SignumValue.fromSigna(fee), 1440, message, null).blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] generateTransactionWithMessage(CrptoNetworks nw, String recipient, byte[] public_key, BigDecimal amount, BigDecimal fee, byte[] message) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				return ns.generateTransactionWithMessage(SignumAddress.fromRs(recipient), public_key, SignumValue.fromSigna(amount), SignumValue.fromSigna(fee), 1440, message, null).blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] signTransaction(CrptoNetworks nw, byte[] privateKey, byte[] unsignedTransaction) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			return SignumCrypto.getInstance().signTransaction(privateKey, unsignedTransaction);
		}
		throw new UnsupportedOperationException();
	}

	public static Object broadcastTransaction(CrptoNetworks nw, byte[] signedTransactionBytes) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				return ns.broadcastTransaction(signedTransactionBytes).blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static final SignumID[] getSignumTxID(CrptoNetworks nw, String address) throws IllegalArgumentException, InterruptedException, ExecutionException {
		Optional<String> opt = get_server_url(nw);
		if (opt.isPresent()) {
			NodeService ns = NodeService.getInstance(opt.get());
			SignumID[] id_arr = new SignumID[] {};
			try {
				id_arr = ns.getAccountTransactionIDs(SignumAddress.fromRs(address)).toFuture().get();
			} catch (IllegalArgumentException | InterruptedException | ExecutionException e) {
				if (e.getCause() != null) {
					if (e.getCause().getClass().getName().equals("signumj.entity.response.http.BRSError")) {
						BRSError e1 = (BRSError) e.getCause();
						if (e1.getCode() == 5) {
							return new SignumID[] {};
						}
					}
				}
				throw e;
			}
			return id_arr;
		}
		return new SignumID[] {};
	}

	public static final Transaction getSignumTx(CrptoNetworks nw, SignumID id) throws InterruptedException, ExecutionException {
		Optional<Transaction> o_tx = Optional.empty();
		try {
			o_tx = MyDb.getSignumTxFromLocal(nw,id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(o_tx.isPresent()) {
			return o_tx.get();
		}else {
			Optional<String> opt = get_server_url(nw);
			if (get_server_url(nw).isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				Transaction tx = ns.getTransaction(id).toFuture().get();
				if(tx!=null) {
					MyDb.putSignumTx(nw,tx);
				}
				return tx;
			}
		}
		throw new InterruptedException();
	}
	
	public static final Optional<String> get_server_url(CrptoNetworks network) {
		Optional<String> opt = MyDb.get_server_url(network);
		if (opt.isEmpty()) {
			List<String> nws = Arrays.asList();
			try {
				nws = IOUtils.readLines(Util.getResourceAsStream("network/" + network.name().toLowerCase() + ".txt"), "UTF-8");
			} catch (IOException e) {
			}
			if (!nws.isEmpty()) {
				MyDb.update_server_url(network, nws.get(0));
				return Optional.of(nws.get(0));
			}
		}
		return opt;
	}
	
}
