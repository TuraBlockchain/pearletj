package hk.zdl.crypto.pearlet.ui;

import hk.zdl.crypto.pearlet.util.Util;

public class AquaMagic {

	public static final void do_trick() {
		var appName = Util.getProp().get("appName");
		System.setProperty("apple.awt.application.name", appName);
		System.setProperty("apple.awt.application.appearance", "system");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
	}
}
