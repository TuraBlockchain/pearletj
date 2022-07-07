package hk.zdl.crypto.pearlet.component.event;

import hk.zdl.crypto.pearlet.util.CrptoNetworks;

public class AccountChangeEvent {

	public final CrptoNetworks network;
	public final String account;

	public AccountChangeEvent(CrptoNetworks network, String account) {
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
