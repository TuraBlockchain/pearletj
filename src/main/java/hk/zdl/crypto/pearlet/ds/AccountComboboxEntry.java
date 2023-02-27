package hk.zdl.crypto.pearlet.ds;

public class AccountComboboxEntry {

	public CryptoNetwork network;
	public String address = null;
	public String nickname = null;

	public AccountComboboxEntry(CryptoNetwork network, String address, String nickname) {
		super();
		this.network = network;
		this.address = address;
		this.nickname = nickname;
	}

}
