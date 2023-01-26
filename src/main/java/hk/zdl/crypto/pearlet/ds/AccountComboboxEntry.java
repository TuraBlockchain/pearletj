package hk.zdl.crypto.pearlet.ds;

import hk.zdl.crypto.pearlet.util.CrptoNetworks;

public class AccountComboboxEntry {

	public CrptoNetworks network;
	public String address = null;
	public String nickname = null;

	public AccountComboboxEntry(CrptoNetworks network, String address, String nickname) {
		super();
		this.network = network;
		this.address = address;
		this.nickname = nickname;
	}

}
