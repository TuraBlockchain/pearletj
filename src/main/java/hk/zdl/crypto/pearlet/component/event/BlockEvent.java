package hk.zdl.crypto.pearlet.component.event;

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;

public class BlockEvent<E> {

	public enum Type {
		START, INSERT, FINISH
	}

	public final CryptoNetwork network;
	public final Type type;
	public final E data;

	public BlockEvent(CryptoNetwork network, Type type, E data) {
		this.network = network;
		this.type = type;
		this.data = data;
	}
}
