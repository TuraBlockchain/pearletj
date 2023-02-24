package hk.zdl.crypto.pearlet.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.SystemTray;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.commons.io.IOUtils;

import com.formdev.flatlaf.util.SystemInfo;

import hk.zdl.crypto.pearlet.component.MyStretchIcon;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.util.Util;

public class UIUtil {

	public static final String get_icon_file_name(CryptoNetwork o) {
		if (o.getType() == CryptoNetwork.Type.WEB3J) {
			return "ethereum-crypto-cryptocurrency-2-svgrepo-com.svg";
		} else {
			var url = o.getUrl();
			try {
				var list = IOUtils.readLines(UIUtil.class.getClassLoader().getResourceAsStream("network/signum.txt"), Charset.defaultCharset());
				if(list.contains(url)) {
					return "Signum_Logomark_black.png";
				}
				list = IOUtils.readLines(UIUtil.class.getClassLoader().getResourceAsStream("network/rotura.txt"), Charset.defaultCharset());
				if(list.contains(url)) {
					return "peth-logo.png";
				}
			} catch (IOException e) {
			}
		}
		return "blockchain-dot-com-svgrepo-com.svg";
	}

	public static final void adjust_table_width(JTable table, TableColumnModel table_column_model) {
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 100; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				try {
					TableCellRenderer renderer = table.getCellRenderer(row, column);
					Component comp = table.prepareRenderer(renderer, row, column);
					width = Math.max(comp.getPreferredSize().width + 1, width);
				} catch (Exception e) {
				}
			}
			if (width > 500)
				width = 500;
			table_column_model.getColumn(column).setMinWidth(width);
			table_column_model.getColumn(column).setPreferredWidth(width);
		}
	}

	public static final void printVersionOnSplashScreen() {
		String text = Util.getAppVersion();
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
			return new MyStretchIcon(ImageIO.read(Util.getResource(path)), w, h);
		} catch (IOException e) {
			return null;
		}
	}

	public static final boolean isAltDown(ActionEvent e) {
		return new KeyEvent((Component) e.getSource(), 0, 0, e.getModifiers(), 0, ' ').isAltDown();
	}

	public static final void displayMessage(String title, String message, MessageType messageType) {
		if (SystemInfo.isMacOS) {
			java.awt.Toolkit.getDefaultToolkit().beep();
		}
		if (SystemInfo.isLinux) {
			try {
				new ProcessBuilder("zenity", "--notification", "--title=" + title, "--text=" + message).start().waitFor();
			} catch (Exception e) {
			}
		} else if (SystemTray.isSupported()) {
			if (SystemTray.getSystemTray().getTrayIcons().length > 0) {
				if (messageType == null) {
					messageType = MessageType.INFO;
				}
				SystemTray.getSystemTray().getTrayIcons()[0].displayMessage(title, message, messageType);
			}
		} else {
			Map<MessageType, Integer> map = new EnumMap<>(MessageType.class);
			map.put(MessageType.NONE, JOptionPane.PLAIN_MESSAGE);
			map.put(MessageType.WARNING, JOptionPane.WARNING_MESSAGE);
			map.put(MessageType.INFO, JOptionPane.INFORMATION_MESSAGE);
			map.put(MessageType.ERROR, JOptionPane.ERROR_MESSAGE);
			JOptionPane.showMessageDialog(null, message, title, map.get(messageType));
		}
	}

	public static final boolean show_confirm_exit_dialog(Component... c) {
		return JOptionPane.showConfirmDialog(c.length > 0 ? c[0] : null, "Are you sure to exit PearlrtJ ?", "Exit", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
	}
}
