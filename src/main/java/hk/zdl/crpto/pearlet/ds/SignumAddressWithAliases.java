package hk.zdl.crpto.pearlet.ds;

import signumj.entity.SignumAddress;

public class SignumAddressWithAliases {

	private final SignumAddress address;
	private String aliases;

	public SignumAddressWithAliases(SignumAddress address) {
		super();
		this.address = address;
	}

	public String getAliases() {
		return aliases;
	}

	public void setAliases(String aliases) {
		this.aliases = aliases;
	}

	public SignumAddress getAddress() {
		return address;
	}

	@Override
	public String toString() {
		if(aliases!=null) {
			return aliases;
		}
		return address.toString();
	}
}
