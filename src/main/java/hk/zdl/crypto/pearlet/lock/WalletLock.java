package hk.zdl.crypto.pearlet.lock;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.greenrobot.eventbus.EventBus;

import hk.zdl.crypto.pearlet.component.event.WalletLockEvent;
import hk.zdl.crypto.pearlet.component.event.WalletTimerEvent;
import hk.zdl.crypto.pearlet.util.Util;

public class WalletLock {

	public static final String AUTO_LOCK_MIN = "AUTO_LOCK_MIN";
	private static final int MIN_PW_LEN = 8;
	private static Timer timer = new Timer();
	private static long last_unlock_time = -1;
	private static long target_lock_time = -1;
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

	public static synchronized boolean unlock() {
		var pw_field = new JPasswordField();
		if (show_option_pane("Enter Password:", frame, pw_field) && validete_password(pw_field.getPassword())) {
			var i = Util.getUserSettings().getInt(WalletLock.AUTO_LOCK_MIN, -1);
			if (i > 0) {
				last_unlock_time = System.currentTimeMillis();
				target_lock_time = last_unlock_time + Duration.ofMinutes(i).toMillis();
			} else {
				last_unlock_time = -1;
				target_lock_time = -1;
			}
			locked = false;
			timer.cancel();
			timer = new Timer();
			timer.scheduleAtFixedRate(getTimerTask(), 0, i <= 1 ? 500 : 2000);
			return true;
		}
		return false;
	}

	public static boolean lock() {
		locked = true;
		timer.cancel();
		EventBus.getDefault().post(new WalletTimerEvent(0, 100));
		return true;
	}

	public static boolean isLocked() {
		return locked;
	}

	private static final TimerTask getTimerTask() {
		return new TimerTask() {

			@Override
			public void run() {
				if (target_lock_time < 0) {
					return;
				} else if (System.currentTimeMillis() < target_lock_time) {
					var a = target_lock_time - last_unlock_time;
					var b = target_lock_time - System.currentTimeMillis();
					var c = 100F * b / a;
					EventBus.getDefault().post(new WalletTimerEvent((int) c, 100));
				} else {
					EventBus.getDefault().post(new WalletTimerEvent(0, 100));
					EventBus.getDefault().post(new WalletLockEvent(WalletLockEvent.Type.LOCK));
					timer.cancel();
				}
			}
		};
	}

	private static boolean validete_password(char[] password) {
		// TODO:implement this!
		return true;
	}

	private static boolean change_password(char[] password) {
		// TODO:implement this!
		return true;
	}
}
