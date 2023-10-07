package hk.zdl.crypto.pearlet.lock;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class WalletLock {

	private static final int MIN_PW_LEN = 8;
	private static boolean locked = false;
	private static Component frame = null;

	public static void setFrame(Component frame) {
		WalletLock.frame = frame;
	}

	public static boolean change_password() {
		var pw_field = new JPasswordField[] { new JPasswordField(), new JPasswordField(), new JPasswordField() };
		if (show_option_pane("Old password:", frame, pw_field[0])) {
			if (!validete_password(pw_field[0].getPassword())) {
				JOptionPane.showMessageDialog(frame, "Wrong Password!", null, JOptionPane.ERROR_MESSAGE);
				return false;
			} else if (show_option_pane("New password:", frame, pw_field[1])) {
				if (pw_field[1].getPassword().length < MIN_PW_LEN) {
					JOptionPane.showMessageDialog(frame, "Password must be at least " + MIN_PW_LEN + " characters!", null, JOptionPane.ERROR_MESSAGE);
					return false;
				} else if (show_option_pane("Re-type new password:", frame, pw_field[2])) {
					if (!Arrays.equals(pw_field[1].getPassword(), pw_field[2].getPassword())) {
						JOptionPane.showMessageDialog(frame, "Password Mismatch!", null, JOptionPane.ERROR_MESSAGE);
						return false;
					} else {
						return change_password(pw_field[2].getPassword());
					}
				}
			}
		}
		return false;
	}

	private static final boolean show_option_pane(String title, Component frame, JPasswordField pwf) {
		var pane = new JOptionPane(pwf, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		var dlg = pane.createDialog(frame, title);
		dlg.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				pwf.grabFocus();
			}
		});
		dlg.setVisible(true);
		return pane.getValue().equals(JOptionPane.OK_OPTION);
	}

	public static boolean unlock() {
		var pw_field = new JPasswordField();
		if (show_option_pane("Enter Password:", frame, pw_field) && validete_password(pw_field.getPassword())) {
			locked = false;
			return true;
		}
		return false;
	}

	private static boolean validete_password(char[] password) {
		//TODO:implement this!
		return true;
	}
	
	private static boolean change_password(char[] password) {
		//TODO:implement this!
		return true;
	}

	public static boolean lock() {
		locked = true;
		return true;
	}

	public static boolean isLocked() {
		return locked;
	}
}
