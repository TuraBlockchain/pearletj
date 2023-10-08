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
		var x = Db.findFirst("select * from networks where ID = ?", r.getInt("NWID"));
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

	public byte[] getPrivateKey() {
		if(WalletLock.isLocked()) {
			if(WalletLock.unlock()) {
				// TODO:
			}else {
				throw new IllegalStateException("Failed to unlock wallet!");
			}
		}
		// TODO:
		return r.getBytes("PRIVATE_KEY");
	}

	public static final Optional<CryptoAccount> getAccount(CryptoNetwork network, String address) {
		Record r = Db.findFirst("select * from ACCOUNTS WHERE NWID = ? AND ADDRESS = ?", network.getId(), address);
		if (r == null) {
			return Optional.empty();
		} else {
			return Optional.of(new CryptoAccount(r));
		}
	}
}
