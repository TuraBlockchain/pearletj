package hk.zdl.crypto.pearlet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;

import hk.zdl.crypto.pearlet.component.AboutPanel;
import hk.zdl.crypto.pearlet.component.AccountInfoPanel;
import hk.zdl.crypto.pearlet.component.AlisesPanel;
import hk.zdl.crypto.pearlet.component.DashBoard;
import hk.zdl.crypto.pearlet.component.NetworkAndAccountBar;
import hk.zdl.crypto.pearlet.component.ReceivePanel;
import hk.zdl.crypto.pearlet.component.SendPanel;
import hk.zdl.crypto.pearlet.component.SettingsPanel;
import hk.zdl.crypto.pearlet.component.TranscationPanel;
import hk.zdl.crypto.pearlet.component.miner.MinerExplorePane;
import hk.zdl.crypto.pearlet.component.plot.PlotPanel;
import hk.zdl.crypto.pearlet.misc.IndepandentWindows;
import hk.zdl.crypto.pearlet.notification.ether.EtherAccountsMonitor;
import hk.zdl.crypto.pearlet.notification.signum.SignumAccountsMonitor;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.tx_history_query.TxHistoryQueryExecutor;
import hk.zdl.crypto.pearlet.ui.AquaMagic;
import hk.zdl.crypto.pearlet.ui.CloseableTabbedPaneLayerUI;
import hk.zdl.crypto.pearlet.ui.GnomeMagic;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;

public class Main {

	public static void main(String[] args) throws Throwable {
		GnomeMagic.do_trick();
		AquaMagic.do_trick();
		UIUtil.printVersionOnSplashScreen();
		Image app_icon = ImageIO.read(Util.getResource("app_icon.png"));
		Util.submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				Taskbar.getTaskbar().setIconImage(app_icon);
				return null;
			}

		});
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
		Util.submit(MyDb::create_missing_tables);
		createFrame(otd, app_icon);
		new TxHistoryQueryExecutor();
		new EtherAccountsMonitor();
		new SignumAccountsMonitor(CrptoNetworks.ROTURA);
		new SignumAccountsMonitor(CrptoNetworks.SIGNUM);
	}

	private static final void createFrame(OsThemeDetector otd, Image app_icon) {
		SwingUtilities.invokeLater(() -> {
			var appName = Util.getProp().get("appName");
			var frame = new JFrame(appName);
			frame.setIconImage(app_icon);
			frame.getContentPane().setLayout(new BorderLayout());
			var panel1 = new JPanel(new BorderLayout());
			var panel2 = new JPanel();
			panel1.add(new NetworkAndAccountBar(), BorderLayout.NORTH);
			panel1.add(panel2, BorderLayout.CENTER);
			frame.add(panel1, BorderLayout.CENTER);

			var mfs = new MainFrameSwitch(panel2);
			mfs.put("dashboard", new DashBoard());
			mfs.put("txs", new TranscationPanel());
			mfs.put("send", new SendPanel());
			mfs.put("rcv", new ReceivePanel());
			mfs.put("acc_info", new AccountInfoPanel());
			mfs.put("plot", new PlotPanel());
			mfs.put("miner", new JLayer<JTabbedPane>(new MinerExplorePane(), new CloseableTabbedPaneLayerUI()));
			mfs.put("alis", new AlisesPanel());
			mfs.put("sets", new SettingsPanel());
			mfs.put("about", new AboutPanel());
			var toolbar = new MyToolbar(mfs);
			toolbar.clickButton("dashboard");
			frame.add(toolbar, BorderLayout.WEST);

			var frame_size = new Dimension(Util.getProp().getInt("default_window_width"), Util.getProp().getInt("default_window_height"));
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setPreferredSize(frame_size);
			frame.setMinimumSize(frame_size);
			frame.setSize(frame_size);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			if (screenSize.getWidth() <= frame.getWidth() || screenSize.getHeight() <= frame.getHeight()) {
				frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			}


			otd.registerListener(isDark -> {
				Stream.of(new FlatLightLaf(), new FlatDarkLaf()).filter(o -> o.isDark() == isDark).forEach(FlatLaf::setup);
				SwingUtilities.invokeLater(() -> {
					SwingUtilities.updateComponentTreeUI(frame);
					IndepandentWindows.iterator().forEachRemaining(SwingUtilities::updateComponentTreeUI);
				});
			});
		});
	}

}
