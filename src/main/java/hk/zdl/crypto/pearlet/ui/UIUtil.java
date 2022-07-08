package hk.zdl.crypto.pearlet.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import hk.zdl.crypto.pearlet.Main;
import hk.zdl.crypto.pearlet.component.MyStretchIcon;
import hk.zdl.crypto.pearlet.util.Util;

public class UIUtil {

	private static final String os = System.getProperty("os.name");
	private static TrayIcon trayIcon;
	static {
	}

	public static final void adjust_table_width(JTable table, TableColumnModel table_column_model) {
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 100; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}
			if (width > 300)
				width = 300;
			table_column_model.getColumn(column).setMinWidth(width);
			table_column_model.getColumn(column).setPreferredWidth(width);
		}
	}

	public static final void printVersionOnSplashScreen() {
		String text = Main.class.getPackage().getImplementationVersion();
		SplashScreen ss = SplashScreen.getSplashScreen();
		if (ss == null) {
			return;
		}
		Graphics2D g = ss.createGraphics();
		g.setPaintMode();
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		g.setColor(Color.white);
		g.drawString("Version: " + text, 350, 430);
		ss.update();
		g.dispose();
	}

	public static final MyStretchIcon getStretchIcon(String path, int w, int h) {
		try {
			return  new MyStretchIcon(ImageIO.read(Util.getResource(path)), w, h);
		} catch (IOException e) {
			return null;
		}
	}

	public static final void displayMessage(String title, String message, MessageType messageType) {
		if (os.contains("Linux")) {
			try {
				new ProcessBuilder("zenity", "--notification", "--title=" + title, "--text=" + message).start();
			} catch (IOException e) {
			}
		} else if (os.contains("Mac")) {
			try {
				new ProcessBuilder("osascript", "-e", "display notification \"" + message + "\"" + " with title \"" + title + "\" with sound \" \"").start();
			} catch (IOException e) {
			}
		} else if (SystemTray.isSupported()) {
			if (trayIcon == null) {
				try {
					trayIcon = new TrayIcon(ImageIO.read(UIUtil.class.getClassLoader().getResource("app_icon.png")));
					trayIcon.setImageAutoSize(true);
					SystemTray.getSystemTray().add(trayIcon);
				} catch (Exception e) {
				}
			}
			if (trayIcon != null) {
				if (messageType == null) {
					messageType = MessageType.INFO;
				}
				trayIcon.displayMessage(title, message, messageType);
//				SystemTray.getSystemTray().remove(trayIcon);
			}
		} else {
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
