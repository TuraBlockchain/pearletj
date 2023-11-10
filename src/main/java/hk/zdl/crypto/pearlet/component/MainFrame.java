package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.util.SystemInfo;

import hk.zdl.crypto.pearlet.MainFrameSwitch;
import hk.zdl.crypto.pearlet.MyToolbar;
import hk.zdl.crypto.pearlet.component.blocks.BlocksPanel;
import hk.zdl.crypto.pearlet.component.event.WalletTimerEvent;
import hk.zdl.crypto.pearlet.component.miner.MinerPanel;
import hk.zdl.crypto.pearlet.component.plot.PlotPanel;
import hk.zdl.crypto.pearlet.component.settings.SettingsPanel;
import hk.zdl.crypto.pearlet.lock.WalletLock;
import hk.zdl.crypto.pearlet.misc.IndepandentWindows;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 6121732572128813921L;
	private Dimension frame_size = new Dimension(Util.getProp().getInt("default_window_width"), Util.getProp().getInt("default_window_height"));
	private JPanel panel1 = new JPanel(new BorderLayout());
	private JPanel panel2 = new JPanel();
	private NetworkAndAccountBar naa_bar = new NetworkAndAccountBar();
	private JProgressBar bar = new JProgressBar();
	private MainFrameSwitch mfs = new MainFrameSwitch(panel2);
	private MyToolbar toolbar = new MyToolbar(mfs);

	public MainFrame(String appName, Image app_icon) {
		super(appName);
		setIconImage(app_icon);
		EventBus.getDefault().register(this);
		setLayout(new BorderLayout());
		add(bar, BorderLayout.SOUTH);
		panel1.add(naa_bar, BorderLayout.NORTH);
		panel1.add(panel2, BorderLayout.CENTER);
		add(panel1, BorderLayout.CENTER);

		mfs.put("dashboard", new DashBoard());
		mfs.put("txs", new TranscationPanel());
		mfs.put("blocks", new BlocksPanel());
		mfs.put("send", new SendPanel());
		mfs.put("rcv", new ReceivePanel());
		mfs.put("acc_info", new AccountInfoPanel());
		mfs.put("plot", new PlotPanel());
		mfs.put("miner", new MinerPanel());
		mfs.put("alis", new AlisePanel());
		mfs.put("sets", new SettingsPanel());
		mfs.put("about", new AboutPanel());
		toolbar.clickButton("dashboard");
		add(toolbar, BorderLayout.WEST);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setPreferredSize(frame_size);
		setMinimumSize(frame_size);
		setSize(frame_size);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		toFront();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (UIUtil.show_confirm_exit_dialog(MainFrame.this)) {
					setVisible(false);
					dispose();
					System.exit(0);
				}
			}
		});
		new TrayIconMenu(app_icon, MainFrame.this);
		FlatDesktop.setQuitHandler((e) -> {
			if (UIUtil.show_confirm_exit_dialog(MainFrame.this)) {
				setVisible(false);
				dispose();
				e.performQuit();
			} else {
				e.cancelQuit();
			}
		});
		var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (screenSize.getWidth() <= getWidth() || screenSize.getHeight() <= getHeight()) {
			setExtendedState(MAXIMIZED_BOTH);
		}
		if (SystemInfo.isMacOS) {
			getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
			getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
			toolbar.setBorder(BorderFactory.createEmptyBorder(naa_bar.getHeight(), 0, 0, 0));
		}
		WalletLock.setFrame(this);
		IndepandentWindows.add(this);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(WalletTimerEvent e) {
		bar.setMaximum(e.total);
		bar.setValue(e.value);
		try {
			int progress = 100 * e.value / e.total;
			if (progress < 1) {
				Taskbar.getTaskbar().setWindowProgressValue(this, 0);
				Taskbar.getTaskbar().setWindowProgressState(this, Taskbar.State.OFF);
			} else {
				Taskbar.getTaskbar().setWindowProgressState(this, Taskbar.State.NORMAL);
				Taskbar.getTaskbar().setWindowProgressValue(this, progress);
			}
		} catch (Exception x) {
		}
	}
}
