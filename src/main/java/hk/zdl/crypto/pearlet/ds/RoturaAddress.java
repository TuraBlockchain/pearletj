package hk.zdl.crypto.pearlet.ds;

import signumj.crypto.SignumCrypto;
import signumj.entity.SignumAddress;
import signumj.entity.SignumID;
import signumj.util.SignumUtils;

public class RoturaAddress {

	public static final String prefix = "TS";
	static {
		SignumUtils.addAddressPrefix(prefix);
	}
	private final String address;
	private final SignumID numericID;
	private byte[] public_key;

	private RoturaAddress(SignumID burstID) {
		this.numericID = burstID;
		this.address = SignumCrypto.getInstance().rsEncode(numericID);
	}

	private RoturaAddress(byte[] public_key) {
		this.public_key = public_key;
		this.numericID = SignumCrypto.getInstance().getAddressFromPublic(public_key).getSignumID();
		this.address = SignumCrypto.getInstance().rsEncode(numericID);
	}

	public byte[] getPublicKey() {
		return public_key;
	}

	public String getFullAddress() {
		if (address == null || address.length() == 0) {
			return "";
		} else {
			return getAddressPrefix() + "-" + address;
		}
	}

	public static final String getAddressPrefix() {
		return prefix;
	}

	public String getExtendedAddress() {
		return SignumCrypto.getInstance().getAddressFromPublic(public_key).getExtendedAddress().replaceFirst(SignumUtils.getAddressPrefix() + "-", getAddressPrefix() + "-");
	}

	public String getID() {
		return numericID.getID();
	}

	@Override
	public String toString() {
		return getFullAddress();
	}

	public static RoturaAddress fromId(SignumID burstID) {
		return new RoturaAddress(burstID);
	}

	public static RoturaAddress fromId(long signedLongId) {
		return new RoturaAddress(SignumID.fromLong(signedLongId));
	}

	public static RoturaAddress fromId(String unsignedLongId) {
		return new RoturaAddress(SignumID.fromLong(unsignedLongId));
	}

	public static RoturaAddress fromRs(String RS) throws IllegalArgumentException {
		return new RoturaAddress(SignumAddress.fromRs(RS).getSignumID());
	}

	public static RoturaAddress fromEither(String input) {
		if (input == null)
			return null;
		try {
			return RoturaAddress.fromId(SignumID.fromLong(input));
		} catch (IllegalArgumentException e1) {
			try {
				return RoturaAddress.fromRs(input);
			} catch (IllegalArgumentException e2) {
				return null;
			}
		}
	}

	public static final RoturaAddress fromPublicKey(byte[] public_key) {
		RoturaAddress r = new RoturaAddress(SignumCrypto.getInstance().getAddressFromPublic(public_key).getSignumID());
		r.public_key = public_key;
		return r;
	}

	public static final RoturaAddress fromPrivateKey(byte[] private_key) {
		return fromPublicKey(SignumCrypto.getInstance().getAddressFromPrivate(private_key).getPublicKey());
	}

	public static final RoturaAddress fromPassPhrase(String passphrase) {
		return fromPublicKey(SignumCrypto.getInstance().getAddressFromPassphrase(passphrase).getPublicKey());
	}

}
