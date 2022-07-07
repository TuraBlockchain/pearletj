package hk.zdl.crpto.pearlet.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import hk.zdl.crpto.pearlet.Main;
import hk.zdl.crpto.pearlet.util.Util;

public class UIUtil {

	private static Object trayIcon = null;
	private static boolean fail_to_load_trayIcon = false;

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
		SplashScreen ss = null;
		try {
			ss = SplashScreen.getSplashScreen();
		} catch (Throwable e) {
		}
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

	public static void displayMessage(String caption, String text, MessageType messageType) {
		if (SystemTray.isSupported()) {
			if (trayIcon == null && !fail_to_load_trayIcon) {
				try {
					trayIcon = new TrayIcon(ImageIO.read(Util.getResource("app_icon.png")));
					((TrayIcon) trayIcon).setImageAutoSize(true);
					SystemTray.getSystemTray().add((TrayIcon) trayIcon);
				} catch (Throwable e) {
					fail_to_load_trayIcon = true;
				}
			}
			if (trayIcon != null) {
				((TrayIcon) trayIcon).displayMessage(caption, text, messageType);
			}
		} else {
			int msg_type = 0;
			switch (messageType) {
			case ERROR:
				msg_type = JOptionPane.ERROR_MESSAGE;
				break;
			case INFO:
				msg_type = JOptionPane.INFORMATION_MESSAGE;
				break;
			case NONE:
				msg_type = JOptionPane.PLAIN_MESSAGE;
				break;
			case WARNING:
				msg_type = JOptionPane.WARNING_MESSAGE;
				break;
			default:
				break;

			}
			JOptionPane.showMessageDialog(null, text, caption, msg_type);
		}
	}
}
