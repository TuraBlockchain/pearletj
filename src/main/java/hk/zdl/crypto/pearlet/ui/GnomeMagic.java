package hk.zdl.crypto.pearlet.ui;

import java.awt.Toolkit;
import java.lang.reflect.Field;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.formdev.flatlaf.util.SystemInfo;

public final class GnomeMagic {

	public static final void do_trick() {
		if (SystemInfo.isLinux) {
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
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
