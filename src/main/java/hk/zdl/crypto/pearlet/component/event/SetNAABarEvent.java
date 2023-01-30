package hk.zdl.crypto.pearlet.component.event;

import hk.zdl.crypto.pearlet.util.CrptoNetworks;

public class SetNAABarEvent {

	private final CrptoNetworks network;
	private final String address;

	public SetNAABarEvent(CrptoNetworks network, String address) {
		this.network = network;
		this.address = address;
	}

	public CrptoNetworks getNetwork() {
		return network;
	}

	public String getAddress() {
		return address;
	}

	@Override
	public String toString() {
		return "SetNAABarEvent [network=" + network + ", address=" + address + "]";
	}
}
