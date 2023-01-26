package hk.zdl.crypto.pearlet.component.event;

import java.math.BigDecimal;

import hk.zdl.crypto.pearlet.util.CrptoNetworks;

public class BalanceUpdateEvent {
	private final CrptoNetworks network;
	private final String address;
	private final BigDecimal balance;

	public BalanceUpdateEvent(CrptoNetworks network, String address, BigDecimal balance) {
		super();
		this.network = network;
		this.address = address;
		this.balance = balance;
	}

	public CrptoNetworks getNetwork() {
		return network;
	}

	public String getAddress() {
		return address;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	@Override
	public String toString() {
		return "BalanceUpdateEvent [network=" + network + ", address=" + address + ", balance=" + balance + "]";
	}
}
