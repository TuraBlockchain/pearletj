package hk.zdl.crypto.pearlet.component;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.event.WalletLockEvent;
import hk.zdl.crypto.pearlet.lock.WalletLock;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class TrayIconMenu implements ItemListener {

	private final CheckboxMenuItem lock_menu_item = new CheckboxMenuItem();
	private final MenuItem quit_menu_item = new MenuItem("Quit");

	public TrayIconMenu(Image app_icon, JFrame frame) {
		EventBus.getDefault().register(this);
		lock_menu_item.addItemListener(this);
		quit_menu_item.addActionListener((e) -> {
			if (UIUtil.show_confirm_exit_dialog(frame)) {
				frame.setVisible(false);
				frame.dispose();
				System.exit(0);
			}
		});
		var menu = new PopupMenu();
		menu.add(lock_menu_item);
		menu.add(quit_menu_item);
		var trayIcon = new TrayIcon(app_icon, Util.getProp().get("appName"), menu);
		trayIcon.setImageAutoSize(true);
		try {
			SystemTray.getSystemTray().add(trayIcon);
		} catch (AWTException x) {
		}

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (lock_menu_item.getState()) {
			if (WalletLock.lock()) {
				EventBus.getDefault().post(new WalletLockEvent(WalletLockEvent.Type.LOCK));
			} else {
				lock_menu_item.setState(false);
			}
		} else {
			if (WalletLock.unlock()) {
				EventBus.getDefault().post(new WalletLockEvent(WalletLockEvent.Type.UNLOCK));
			} else {
				lock_menu_item.setState(true);
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(WalletLockEvent e) {
		if (e.type == WalletLockEvent.Type.LOCK) {
			lock_menu_item.setLabel("Locked");
			lock_menu_item.setState(true);
		} else {
			lock_menu_item.setLabel("Unlocked");
			lock_menu_item.setState(false);
		}
	}

}
