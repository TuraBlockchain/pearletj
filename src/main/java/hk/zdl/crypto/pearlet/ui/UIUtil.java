package hk.zdl.crypto.pearlet.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;

import javax.imageio.ImageIO;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import hk.zdl.crypto.pearlet.Main;

public class UIUtil {
	
//	private static TrayIcon trayIcon;
//	static {
//		try {
//			trayIcon = new TrayIcon(ImageIO.read(UIUtil.class.getClassLoader().getResource("app_icon.png")));
//			trayIcon.setImageAutoSize(true);
//			SystemTray.getSystemTray().add(trayIcon);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

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
	
//	public static void displayMessage(String caption, String text, MessageType messageType) {
//		trayIcon.displayMessage(caption, text, messageType);
//	}
}
