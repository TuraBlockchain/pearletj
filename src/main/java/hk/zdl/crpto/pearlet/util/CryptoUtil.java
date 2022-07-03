package hk.zdl.crpto.pearlet.util;

import signumj.crypto.SignumCrypto;

public class CryptoUtil {

	public static final boolean validateAccount(String network, String type, String text) {
		SignumCrypto.getInstance();

		return false;
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
			}
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
