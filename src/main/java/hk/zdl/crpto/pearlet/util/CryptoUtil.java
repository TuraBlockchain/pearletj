package hk.zdl.crpto.pearlet.util;

import static hk.zdl.crpto.pearlet.util.CrptoNetworks.SIGNUM;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import hk.zdl.crpto.pearlet.persistence.MyDb;
import signumj.crypto.SignumCrypto;
import signumj.entity.SignumAddress;
import signumj.entity.SignumID;
import signumj.entity.response.Transaction;
import signumj.service.NodeService;

public class CryptoUtil {

	public static final byte[] getPublicKeyFromAddress(CrptoNetworks network, String addr) {
		if (network.equals(SIGNUM)) {
			return SignumAddress.fromRs(addr).getPublicKey();
		}
		throw new UnsupportedOperationException();
	}

	public static final byte[] getPublicKey(CrptoNetworks network, String type, String text) {
		if (network.equals(SIGNUM)) {
			if (type.equalsIgnoreCase("phrase")) {
				return SignumCrypto.getInstance().getPublicKey(text);
			}
		}
		throw new UnsupportedOperationException();
	}

	public static final byte[] getPrivateKey(CrptoNetworks network, String type, String text) {
		if (network.equals(SIGNUM)) {
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
		if (network.equals(SIGNUM)) {
			return SignumCrypto.getInstance().getPublicKey(private_key);
		}
		throw new UnsupportedOperationException();
	}

	public static final String getAddress(CrptoNetworks network, byte[] public_key) {
		if (network.equals(SIGNUM)) {
			return SignumCrypto.getInstance().getAddressFromPublic(public_key).getFullAddress();
		}
		return null;
	}

	public static final BigDecimal getBalance(CrptoNetworks network, String address) throws Exception {
		if (network.equals(SIGNUM)) {
			Optional<String> opt = MyDb.get_server_url(network);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				return ns.getAccount(SignumAddress.fromRs(address)).toFuture().get().getBalance().toSigna();
			}
			;
		}
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	public static final <E> List<E> getTranscations(CrptoNetworks network, String address) throws Exception {
		if (network.equals(SIGNUM)) {
			Optional<String> opt = MyDb.get_server_url(network);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				SignumID[] id_arr = new SignumID[] {};
				try {
					id_arr = ns.getAccountTransactionIDs(SignumAddress.fromRs(address)).toFuture().get();
				} catch (Exception e) {
				}
				List<Transaction> list = new ArrayList<>();
				for (int i = 0; i < id_arr.length; i++) {
					Transaction tx = ns.getTransaction(id_arr[i]).toFuture().get();
					list.add(tx);
				}
				return (List<E>) list;
			}
		}
		throw new UnsupportedOperationException();
	}

}
