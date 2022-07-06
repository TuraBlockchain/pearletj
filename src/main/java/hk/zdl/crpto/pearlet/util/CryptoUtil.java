package hk.zdl.crpto.pearlet.util;

import static hk.zdl.crpto.pearlet.util.CrptoNetworks.ROTURA;
import static hk.zdl.crpto.pearlet.util.CrptoNetworks.SIGNUM;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import hk.zdl.crpto.pearlet.ds.RoturaAddress;
import hk.zdl.crpto.pearlet.persistence.MyDb;
import signumj.crypto.SignumCrypto;
import signumj.entity.SignumAddress;
import signumj.entity.SignumID;
import signumj.entity.response.Transaction;
import signumj.entity.response.http.BRSError;
import signumj.service.NodeService;

public class CryptoUtil {

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
		Optional<String> opt = get_server_url(nw);
		if (opt.isPresent()) {
			NodeService ns = NodeService.getInstance(opt.get());
			Transaction tx = ns.getTransaction(id).toFuture().get();
			return tx;
		}
		throw new InterruptedException();
	}

	public static final Optional<String> get_server_url(CrptoNetworks network) {
		Optional<String> opt = MyDb.get_server_url(network);
		if (opt.isEmpty()) {
			List<String> nws = Arrays.asList();
			try {
				nws = IOUtils.readLines(CryptoUtil.class.getClassLoader().getResourceAsStream("network/" + network.name().toLowerCase() + ".txt"), "UTF-8");
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
