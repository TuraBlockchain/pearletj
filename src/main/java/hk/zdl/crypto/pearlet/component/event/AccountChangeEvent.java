package hk.zdl.crypto.pearlet.component.event;

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;

public class AccountChangeEvent {

	public final CryptoNetwork network;
	public final String account;

	public AccountChangeEvent(CryptoNetwork network, String account) {
		this.network = network;
		this.account = account;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AccountChangeEvent [network=").append(network).append(", account=").append(account).append("]");
		return builder.toString();
	}
}
