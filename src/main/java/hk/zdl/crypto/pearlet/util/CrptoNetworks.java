package hk.zdl.crypto.pearlet.util;

public enum CrptoNetworks {

	ROTURA, SIGNUM, WEB3J;

	@Override
	public String toString() {
		switch(this) {
		case ROTURA:
			return "PP";
		case SIGNUM:
			return "Signum";
		case WEB3J:
			return "Ethereum";
		default:
			return "";
		}
	}
}
