package hk.zdl.crpto.pearlet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Taskbar;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXFrame;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;

import hk.zdl.crpto.pearlet.component.AboutPanel;
import hk.zdl.crpto.pearlet.component.DashBoard;
import hk.zdl.crpto.pearlet.component.MessagesPanel;
import hk.zdl.crpto.pearlet.component.NetworkAndAccountBar;
import hk.zdl.crpto.pearlet.component.ReceivePanel;
import hk.zdl.crpto.pearlet.component.SendPanel;
import hk.zdl.crpto.pearlet.component.SettingsPanel;
import hk.zdl.crpto.pearlet.component.TranscationPanel;
import hk.zdl.crpto.pearlet.misc.IndepandentWindows;
import hk.zdl.crpto.pearlet.persistence.MyDb;
import hk.zdl.crpto.pearlet.tx_history_query.TxHistoryQueryExecutor;
import hk.zdl.crpto.pearlet.ui.UIUtil;
import hk.zdl.crpto.pearlet.util.Util;

public class Main {

	public static void main(String[] args) throws Throwable {
		var appName = Util.getProp().get("appName");
		System.setProperty("apple.awt.application.name", appName);
		System.setProperty("apple.awt.application.appearance", "system");
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		UIUtil.printVersionOnSplashScreen();

		Taskbar.getTaskbar().setIconImage(ImageIO.read(Main.class.getClassLoader().getResource("app_icon.png")));
		var otd = OsThemeDetector.getDetector();
		UIManager.setLookAndFeel(otd.isDark() ? new FlatDarkLaf() : new FlatLightLaf());
		try {
			MyDb.getTables();
		} catch (Throwable x) {
			while (x.getCause() != null) {
				x = x.getCause();
			}
			JOptionPane.showMessageDialog(null, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		SwingUtilities.invokeLater(() -> {
			var frame = new JXFrame(appName);
			frame.getContentPane().setLayout(new BorderLayout());
			var panel1 = new JPanel(new BorderLayout());
			var panel2 = new JPanel();
			var mfs = new MainFrameSwitch(panel2);
			panel1.add(new NetworkAndAccountBar(), BorderLayout.NORTH);
			panel1.add(panel2, BorderLayout.CENTER);
			frame.add(panel1, BorderLayout.CENTER);
			var toolbar = new MyToolbar(mfs);
			frame.add(toolbar, BorderLayout.WEST);

			var frame_size = new Dimension(Util.getProp().getInt("default_window_width"), Util.getProp().getInt("default_window_height"));
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setPreferredSize(frame_size);
			frame.setMinimumSize(frame_size);
			frame.setSize(frame_size);
			frame.pack();
//			frame.setLocationByPlatform(true);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

			mfs.put("dashboard", new DashBoard());
			mfs.put("txs", new TranscationPanel());
			mfs.put("send", new SendPanel());
			mfs.put("rcv", new ReceivePanel());
			mfs.put("msgs", new MessagesPanel());
			mfs.put("sets", new SettingsPanel());
			mfs.put("about", new AboutPanel());

			SwingUtilities.invokeLater(() -> toolbar.clickButton("dashboard"));

			otd.registerListener(isDark -> {
				SwingUtilities.invokeLater(() -> {

					if (isDark) {
						FlatDarkLaf.setup();
					} else {
						FlatLightLaf.setup();
					}
				});
				SwingUtilities.invokeLater(() -> {
					SwingUtilities.updateComponentTreeUI(frame);
					mfs.components().forEach(SwingUtilities::updateComponentTreeUI);
					IndepandentWindows.iterator().forEachRemaining(SwingUtilities::updateComponentTreeUI);
				});
			});
		});
		Util.submit(MyDb::create_missing_tables);
		new TxHistoryQueryExecutor();
	}


}
