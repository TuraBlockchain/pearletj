package hk.zdl.crypto.pearlet.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import hk.zdl.crypto.pearlet.util.Util;

public class WatchAddressCellRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static BufferedImage img = null;
	static {
		try {
			img = ImageIO.read(Util.getResource("icon/" + "eyeglasses.svg"));
		} catch (IOException e) {
		}
	}
	private boolean is_watch_account = false;

	public WatchAddressCellRenderer() {
		setHorizontalAlignment(SwingConstants.RIGHT);
		setIcon(new EmptyIcon(16, 16));
	}

	@Override
	public void paint(Graphics g) {
		int h = getSize().height;
		int o = h / 2 - 8;
		super.paint(g);
		if (is_watch_account && img != null) {
			g.drawImage(img, o, o, 16, 16, null);
		}
	}

	@Override
	protected void setValue(Object value) {
		String str = String.valueOf(value);
		if (str.endsWith(",watch")) {
			str = str.replace(",watch", "");
			is_watch_account = true;
		} else {
			is_watch_account = false;
		}
		super.setValue(str);
	}

	private static final class EmptyIcon implements Icon {

		private final int width, height;

		public EmptyIcon(int width, int height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {

		}

		@Override
		public int getIconWidth() {
			return width;
		}

		@Override
		public int getIconHeight() {
			return height;
		}
	}
}
