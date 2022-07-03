package hk.zdl.crpto.pearlet.util;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import signumj.crypto.SignumCrypto;
import signumj.entity.SignumAddress;

public class CryptoUtil {
	
	public static final byte[] getPublicKeyFromAddress(String network, String addr) {
		if (network.equals("signum")) {
			return SignumAddress.fromRs(addr).getPublicKey();
		}
		throw new UnsupportedOperationException();
	}

	public static final byte[] getPublicKey(String network, String type, String text) {
		if (network.equals("signum")) {
			if (type.equalsIgnoreCase("phrase")) {
				return SignumCrypto.getInstance().getPublicKey(text);
			}
		}
		throw new UnsupportedOperationException();
	}

	public static final byte[] getPrivateKey(String network, String type, String text) {
		if (network.equals("signum")) {
			if (type.equalsIgnoreCase("phrase")) {
				return SignumCrypto.getInstance().getPrivateKey(text);
			}else if(type.equalsIgnoreCase("base64")) {
				return Base64.decode(text);
			}else if(type.equals("HEX")) {
				return Hex.decode(text);
			}
		}
		throw new UnsupportedOperationException();
	}
	

	public static final byte[] getPublicKey(String network, byte[] private_key) {
		if (network.equals("signum")) {
			return SignumCrypto.getInstance().getPublicKey(private_key);
		}
		throw new UnsupportedOperationException();
	}
	
	public static final String getAddress(String network,byte[] public_key) {
		if (network.equals("signum")) {
			return SignumCrypto.getInstance().getAddressFromPublic(public_key).getFullAddress();
		}
		return null;
	}

}
