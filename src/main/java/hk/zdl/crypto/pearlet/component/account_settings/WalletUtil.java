package hk.zdl.crypto.pearlet.component.account_settings;

import java.nio.charset.Charset;
import java.util.ResourceBundle;

import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Numeric;

import hk.zdl.crypto.pearlet.component.account_settings.burst.PKT;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.lock.WalletLock;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class WalletUtil {

	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	public static final boolean insert_web3j_account(CryptoNetwork nw, ECKeyPair eckp) throws Exception {
		boolean b = false;
		var private_key = Numeric.toBytesPadded(eckp.getPrivateKey(), 32);
		var public_key = Numeric.toBytesPadded(eckp.getPublicKey(), 64);
		var address = CryptoUtil.getAddress(nw, public_key);
		if (WalletLock.hasPassword()) {
			var o = WalletLock.unlock();
			if (o.isPresent()) {
				if (o.get()) {
					var enc_pvk = WalletLock.encrypt_private_key(private_key);
					private_key = new byte[] { 1 };
					b = MyDb.insert_or_update_account(nw, address, public_key, private_key);
					var account_id = MyDb.getAccount(nw, address).get().getInt("ID");
					MyDb.insert_or_update_encpvk(nw.getId(), account_id, enc_pvk);
				} else {
					throw new IllegalArgumentException(rsc_bdl.getString("TRAY.WRONG_PW"));
				}
			} else {
				throw new IllegalStateException(rsc_bdl.getString("SETTINGS.LOCK.IS_NOW_LOCKED"));
			}
		} else {
			b = MyDb.insert_or_update_account(nw, address, public_key, private_key);
		}
		return b;
	}

	public static final boolean insert_burst_account(CryptoNetwork nw, PKT type, String text) throws Exception {
		var private_key = CryptoUtil.getPrivateKey(nw, type, text);
		var public_key = CryptoUtil.getPublicKey(nw, private_key);
		var address = CryptoUtil.getAddress(nw, public_key);
		if (WalletLock.hasPassword()) {
			var o = WalletLock.unlock();
			if (o.isPresent()) {
				if (o.get()) {
					private_key = WalletLock.encrypt_private_key(private_key);
					if (MyDb.insert_or_update_account(nw, address, public_key, new byte[] { 1 })) {
						var account_id = MyDb.getAccount(nw, address).get().getInt("ID");
						if (MyDb.insert_or_update_encpvk(nw.getId(), account_id, private_key)) {
							if (type == PKT.Phrase) {
								var enc_pse = WalletLock.encrypt_private_key(Charset.defaultCharset().encode(text).array());
								return MyDb.insert_or_update_encpse(nw.getId(), account_id, enc_pse);
							}
						}
					}
				} else {
					throw new IllegalArgumentException(rsc_bdl.getString("TRAY.WRONG_PW"));
				}
			} else {
				throw new IllegalStateException(rsc_bdl.getString("SETTINGS.LOCK.IS_NOW_LOCKED"));
			}
		} else {
			return MyDb.insert_or_update_account(nw, address, public_key, private_key);
		}
		return false;
	}
}
