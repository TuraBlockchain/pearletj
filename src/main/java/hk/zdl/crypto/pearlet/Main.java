package hk.zdl.crypto.pearlet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Taskbar;
import java.awt.Taskbar.Feature;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.derby.shared.common.error.StandardException;
import org.json.JSONArray;
import org.json.JSONTokener;

import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.util.SystemInfo;

import hk.zdl.crypto.pearlet.component.AboutPanel;
import hk.zdl.crypto.pearlet.component.AccountInfoPanel;
import hk.zdl.crypto.pearlet.component.AlisesPanel;
import hk.zdl.crypto.pearlet.component.DashBoard;
import hk.zdl.crypto.pearlet.component.NetworkAndAccountBar;
import hk.zdl.crypto.pearlet.component.ReceivePanel;
import hk.zdl.crypto.pearlet.component.SendPanel;
import hk.zdl.crypto.pearlet.component.TranscationPanel;
import hk.zdl.crypto.pearlet.component.miner.MinerPanel;
import hk.zdl.crypto.pearlet.component.plot.PlotPanel;
import hk.zdl.crypto.pearlet.component.settings.SettingsPanel;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.laf.MyUIManager;
import hk.zdl.crypto.pearlet.misc.IndepandentWindows;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.tx_history_query.TxHistoryQueryExecutor;
import hk.zdl.crypto.pearlet.ui.AquaMagic;
import hk.zdl.crypto.pearlet.ui.GnomeMagic;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.NWMon;
import hk.zdl.crypto.pearlet.util.Util;

public class Main {

	public static void main(String[] args) throws Throwable {
		AquaMagic.do_trick();
		GnomeMagic.do_trick();
		var app_icon = ImageIO.read(Util.getResource("app_icon.png"));
		if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Feature.ICON_IMAGE)) {
			Taskbar.getTaskbar().setIconImage(app_icon);
		}
		UIUtil.printVersionOnSplashScreen();
		MyUIManager.setLookAndFeel();
		var db_empty = is_db_empty();
		try {
			System.setProperty("derby.system.home", Files.createTempDirectory(null).toFile().getAbsolutePath());
			MyDb.create_missing_tables();
		} catch (Throwable x) {
			while (x.getCause() != null && x.getCause() != x) {
				x = x.getCause();
			}
			var msg = x.getLocalizedMessage();
			if (x.getClass().equals(StandardException.class)) {
				if (((StandardException) x).getSQLState().equals("XSDB6")) {
					msg = "Only one instance can run concurrently.";
				}
			}
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		if (db_empty) {
			create_default_networks();
		}
		createFrame(app_icon);
		new NWMon();
		new TxHistoryQueryExecutor();
		Util.submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				var jarr = new JSONArray(new JSONTokener(Main.class.getClassLoader().getResourceAsStream("network/predefined.json")));
				var jobj = jarr.getJSONObject(0);
				var name = jobj.getString("network name");
				var url = jobj.getString("server url");
				MyDb.get_networks().stream().filter(n -> n.getUrl().equals("http://mainnet.peth.world:6876")).findFirst().ifPresent(nw -> {
					nw.setName(name);
					nw.setUrl(url);
					MyDb.update_network(nw);
				});
				return null;
			}
		});
	}

	private static void create_default_networks() {
		var jarr = new JSONArray(new JSONTokener(Main.class.getClassLoader().getResourceAsStream("network/predefined.json")));
		for (var i = 0; i < jarr.length(); i++) {
			var jobj = jarr.getJSONObject(i);
			if (jobj.optBoolean("add by default")) {
				var new_network = new CryptoNetwork();
				new_network.setName(jobj.getString("network name"));
				new_network.setUrl(jobj.getString("server url"));
				new_network.setType(CryptoNetwork.Type.BURST);
				MyDb.insert_network(new_network);
			}
		}
	}

	private static boolean is_db_empty() throws Exception {
		var db_path = new File(Util.getUserDataDir()).toPath();
		if (!Files.exists(db_path)) {
			return true;
		} else if (Files.list(db_path).filter(p -> Files.isRegularFile(p) || Files.isDirectory(p)).count() < 1) {
			return true;
		}
		return false;
	}

	private static final void createFrame(Image app_icon) {
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
				TrayIcon trayIcon = new TrayIcon(ImageIO.read(Util.getResource("app_icon.png")), Util.getProp().get("appName"), menu);
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
