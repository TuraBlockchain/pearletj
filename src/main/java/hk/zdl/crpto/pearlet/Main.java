package hk.zdl.crpto.pearlet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Taskbar;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXFrame;

import com.formdev.flatlaf.FlatLightLaf;

import hk.zdl.crpto.pearlet.component.DashBoard;
import hk.zdl.crpto.pearlet.component.NetworkAndAccountBar;
import hk.zdl.crpto.pearlet.component.ReceivePanel;
import hk.zdl.crpto.pearlet.component.TranscationPanel;

public class Main {

	public static void main(String[] args) throws Throwable {
		System.setProperty("apple.awt.application.name", "Pearlet");
		System.setProperty("apple.awt.application.appearance", "system");
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		SwingUtilities.invokeLater(() -> {
			FlatLightLaf.setup();

			var frame = new JXFrame("Pearlet");
			frame.getContentPane().setLayout(new BorderLayout());
			var panel1 = new JPanel(new BorderLayout());
			var panel2 = new JPanel();
			var mfs = new MainFrameSwitch(panel2);
			panel1.add(new NetworkAndAccountBar(), BorderLayout.NORTH);
			panel1.add(panel2, BorderLayout.CENTER);
			frame.add(panel1, BorderLayout.CENTER);
			var toolbar = new MyToolbar(mfs);
			frame.add(toolbar, BorderLayout.WEST);

			var frame_size = new Dimension(800, 600);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setPreferredSize(frame_size);
			frame.setMinimumSize(frame_size);
			frame.setSize(frame_size);
			frame.setLocationByPlatform(true);
			frame.setVisible(true);

			mfs.put("dashboard", new DashBoard());
			mfs.put("txs", new TranscationPanel());
			mfs.put("rcv", new ReceivePanel());

			mfs.showComponent("dashboard");
		});
		SwingUtilities.invokeLater(() -> {
			try {
				Taskbar.getTaskbar().setIconImage(ImageIO.read(Main.class.getClassLoader().getResource("app_icon.png")));
			} catch (IOException e) {
			}
		});

	}

}
