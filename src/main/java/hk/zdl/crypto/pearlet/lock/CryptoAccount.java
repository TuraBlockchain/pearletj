package hk.zdl.crypto.pearlet.lock;

import java.util.Arrays;
import java.util.Optional;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.component.account_settings.burst.PKT;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.CryptoUtil;

public class CryptoAccount {

	private final Record r;

	private CryptoAccount(Record r) {
		this.r = r;
	}

	public CryptoNetwork getNetwork() {
		var x = Db.findFirst("SELECT * FROM NETWORKS WHERE ID = ?", r.getInt("NWID"));
		var c = new CryptoNetwork();
		c.setId(r.getInt("NWID"));
		c.setUrl(x.getStr("URL"));
		c.setName(x.getStr("NWNAME"));
		c.setType(CryptoNetwork.Type.valueOf(x.getStr("NETWORK")));
		return c;
	}

	public String getAddress() {
		return r.getStr("ADDRESS");
	}

	public byte[] getPublicKey() {
		return r.getBytes("PUBLIC_KEY");
	}

	public synchronized byte[] getPrivateKey() throws Exception {
		if (WalletLock.hasPassword()) {
			if (WalletLock.isLocked()) {
				var o = WalletLock.unlock();
				if (!o.isPresent() || o.get() == false) {
					throw new IllegalStateException("Failed to unlock wallet!");
				}
			}
			var network_id = r.getInt("NWID");
			var account_id = r.getInt("ID");
			var network = getNetwork();
			var private_key = WalletLock.decrypt_private_key(network_id, account_id);
			var public_key = CryptoUtil.getPublicKey(network, private_key);
			if (!Arrays.equals(public_key, getPublicKey())) {
				var passphrase = WalletLock.decrypt_passphrase(network_id, account_id);
				if (passphrase == null) {
					MyDb.delete_encpvk(network_id, account_id);
				}else {
					private_key = CryptoUtil.getPrivateKey(network, PKT.Phrase, passphrase);
					public_key = CryptoUtil.getPublicKey(network, private_key);
					if (Arrays.equals(public_key, getPublicKey())) {
						var encpvk = WalletLock.encrypt_private_key(private_key);
						MyDb.insert_or_update_encpvk(network_id, account_id, encpvk);
						return private_key;
					} else {
						MyDb.delete_encpse(network_id, account_id);
					}
				}
				throw new IllegalArgumentException();
			}
			return private_key;
		} else {
			return r.getBytes("PRIVATE_KEY");
		}
	}

	public static final Optional<CryptoAccount> getAccount(CryptoNetwork network, String address) {
		var r = Db.findFirst("SELECT * from ACCOUNTS WHERE NWID = ? AND ADDRESS = ?", network.getId(), address);
		if (r == null) {
			return Optional.empty();
		} else {
			return Optional.of(new CryptoAccount(r));
		}
	}
}
