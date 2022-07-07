package hk.zdl.crypto.pearlet.ui;

import java.awt.Toolkit;
import java.lang.reflect.Field;

public final class GnomeMagic {

	public static final void do_trick() {
		if (System.getProperty("os.name").toLowerCase().contains("linux")) {
			try {
				Toolkit xToolkit = Toolkit.getDefaultToolkit();
				Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
				awtAppClassNameField.setAccessible(true);
				awtAppClassNameField.set(xToolkit, "");
			} catch (Exception ignored) {
			}
		}
	}
}
