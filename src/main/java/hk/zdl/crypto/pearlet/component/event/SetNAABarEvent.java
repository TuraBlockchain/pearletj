package hk.zdl.crypto.pearlet.component.event;

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;

public class SetNAABarEvent {

	private final CryptoNetwork network;
	private final String address;

	public SetNAABarEvent(CryptoNetwork network, String address) {
		this.network = network;
		this.address = address;
	}

	public CryptoNetwork getNetwork() {
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
