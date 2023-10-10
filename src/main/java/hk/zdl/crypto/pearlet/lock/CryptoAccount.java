package hk.zdl.crypto.pearlet.lock;

import java.util.Optional;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;

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

	public byte[] getPrivateKey() throws Exception {
		if (WalletLock.hasPassword()) {
			if (WalletLock.isLocked()) {
				var o = WalletLock.unlock();
				if (!o.isPresent() || o.get() == false) {
					throw new IllegalStateException("Failed to unlock wallet!");
				}
			}
			return WalletLock.decrypt_private_key(r.getInt("NWID"), r.getInt("ID"));
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
