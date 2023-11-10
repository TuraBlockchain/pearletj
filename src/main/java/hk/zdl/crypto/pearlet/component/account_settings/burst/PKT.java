package hk.zdl.crypto.pearlet.component.account_settings.burst;

import hk.zdl.crypto.pearlet.util.Util;

public enum PKT {
	Phrase, HEX, Base64;

	public String toString() {
		return Util.getResourceBundle().getString("GENERAL.PKT." + name());
	}
}
