package hk.zdl.crypto.pearlet.ds;

public class AddressWithNickname {

	public String address = null;
	public String nickname = null;

	public AddressWithNickname(String address) {
		this.address = address;
	}

	public AddressWithNickname(String address, String nickname) {
		this.address = address;
		this.nickname = nickname;
	}

	@Override
	public String toString() {
		if (nickname == null) {
			return address;
		}
		return nickname;
	}
}
