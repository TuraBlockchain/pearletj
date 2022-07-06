package hk.zdl.crpto.pearlet.component.event;

import hk.zdl.crpto.pearlet.util.CrptoNetworks;

public class TxHistoryEvent<E> {

	public enum Type {
		START, INSERT, FINISH
	}

	public final CrptoNetworks network;
	public final Type type;
	public final E data;

	public TxHistoryEvent(CrptoNetworks network, Type type, E data) {
		this.network = network;
		this.type = type;
		this.data = data;
	}
}
