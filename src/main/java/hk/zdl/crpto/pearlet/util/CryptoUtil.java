package hk.zdl.crpto.pearlet.util;

import static hk.zdl.crpto.pearlet.util.CrptoNetworks.*;
import java.math.BigDecimal;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import signumj.crypto.SignumCrypto;
import signumj.entity.SignumAddress;

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
			}else if(type.equalsIgnoreCase("base64")) {
				return Base64.decode(text);
			}else if(type.equals("HEX")) {
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
	
	public static final String getAddress(CrptoNetworks network,byte[] public_key) {
		if (network.equals(SIGNUM)) {
			return SignumCrypto.getInstance().getAddressFromPublic(public_key).getFullAddress();
		}
		return null;
	}
	
	public static final BigDecimal getBalance(CrptoNetworks network,String address) {
		if (network.equals(SIGNUM)) {
			
		}
		throw new UnsupportedOperationException();
	}

}
