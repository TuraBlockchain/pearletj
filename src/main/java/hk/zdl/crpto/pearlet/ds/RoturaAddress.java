package hk.zdl.crpto.pearlet.ds;

import signumj.crypto.SignumCrypto;
import signumj.util.SignumUtils;

public class RoturaAddress {

	public static final String prefix = "TS";
	static {
		SignumUtils.addAddressPrefix(prefix);
	}

	private String address;
	private byte[] public_key;

	public RoturaAddress(String address) {
		if (address.startsWith(prefix + "-") && address.length() == prefix.length() + 21) {
			this.address = address;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public RoturaAddress(byte[] public_key) {
		this.public_key = public_key;
		address = SignumCrypto.getInstance().getAddressFromPublic(public_key).getFullAddress().replaceFirst(SignumUtils.getAddressPrefix() + "-", prefix + "-");
	}

	public byte[] getPublicKey() {
		return public_key;
	}

	public String getID() {
		return SignumCrypto.getInstance().getAddressFromPublic(public_key).getID();
	}

	@Override
	public String toString() {
		return address;
	}

	public static final RoturaAddress fromPrivateKey(byte[] private_key) {
		return new RoturaAddress(SignumCrypto.getInstance().getPublicKey(private_key));
	}

	public static final RoturaAddress fromPassPhase(String passphase) {
		return new RoturaAddress(SignumCrypto.getInstance().getPublicKey(SignumCrypto.getInstance().getPrivateKey(passphase)));
	}

}
