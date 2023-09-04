package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.util.SystemInfo;

import hk.zdl.crypto.pearlet.MainFrameSwitch;
import hk.zdl.crypto.pearlet.MyToolbar;
import hk.zdl.crypto.pearlet.component.blocks.BlocksPanel;
import hk.zdl.crypto.pearlet.component.miner.MinerPanel;
import hk.zdl.crypto.pearlet.component.plot.PlotPanel;
import hk.zdl.crypto.pearlet.component.settings.SettingsPanel;
import hk.zdl.crypto.pearlet.misc.IndepandentWindows;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class MainFrame {

	public static void create(Image app_icon) throws Exception{
		SwingUtilities.invokeLater(() -> {
			var appName = Util.getProp().get("appName");
			var frame = new JFrame(appName);
			frame.setIconImage(app_icon);
			frame.getContentPane().setLayout(new BorderLayout());
			var panel1 = new JPanel(new BorderLayout());
			var panel2 = new JPanel();
			var naa_bar = new NetworkAndAccountBar();
			panel1.add(naa_bar, BorderLayout.NORTH);
			panel1.add(panel2, BorderLayout.CENTER);
			frame.add(panel1, BorderLayout.CENTER);

			var mfs = new MainFrameSwitch(panel2);
			mfs.put("dashboard", new DashBoard());
			mfs.put("txs", new TranscationPanel());
			mfs.put("blocks", new BlocksPanel());
			mfs.put("send", new SendPanel());
			mfs.put("rcv", new ReceivePanel());
			mfs.put("acc_info", new AccountInfoPanel());
			mfs.put("plot", new PlotPanel());
			mfs.put("miner", new MinerPanel());
			mfs.put("alis", new AlisesPanel());
			mfs.put("sets", new SettingsPanel());
			mfs.put("about", new AboutPanel());
			var toolbar = new MyToolbar(mfs);
			toolbar.clickButton("dashboard");
			frame.add(toolbar, BorderLayout.WEST);

			var frame_size = new Dimension(Util.getProp().getInt("default_window_width"), Util.getProp().getInt("default_window_height"));
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.setPreferredSize(frame_size);
			frame.setMinimumSize(frame_size);
			frame.setSize(frame_size);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			frame.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent e) {
					if (UIUtil.show_confirm_exit_dialog(frame)) {
						System.exit(0);
					}
				}
			});
			try {
				var quit_menu_item = new MenuItem("Quit");
				quit_menu_item.addActionListener((e) -> {
					if (UIUtil.show_confirm_exit_dialog(frame)) {
						System.exit(0);
					}
				});
				var menu = new PopupMenu();
				menu.add(quit_menu_item);
				TrayIcon trayIcon = new TrayIcon(app_icon, Util.getProp().get("appName"), menu);
				trayIcon.setImageAutoSize(true);
				SystemTray.getSystemTray().add(trayIcon);
			} catch (Exception e) {
			}
			FlatDesktop.setQuitHandler((e) -> {
				if (UIUtil.show_confirm_exit_dialog(frame)) {
					e.performQuit();
				} else {
					e.cancelQuit();
				}
			});
			var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			if (screenSize.getWidth() <= frame.getWidth() || screenSize.getHeight() <= frame.getHeight()) {
				frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			}
			if (SystemInfo.isMacOS) {
				frame.getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
				frame.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
				toolbar.setBorder(BorderFactory.createEmptyBorder(naa_bar.getHeight(), 0, 0, 0));
			}

			IndepandentWindows.add(frame);
		});
		
	}
}
