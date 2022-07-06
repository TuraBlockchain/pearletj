package hk.zdl.crpto.pearlet.component;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;

@SuppressWarnings("serial")
public class MyStretchIcon extends ImageIcon {

	private final int width, height;

	public MyStretchIcon(Image image, int width, int height) {
		super(image);
		if (height < 0) {
			this.width = width;
			this.height = (int) (1.0 * width / image.getWidth(null) * image.getHeight(null));
		} else if (width < 0) {
			this.height = height;
			this.width = (int) (1.0 * height / image.getHeight(null) * image.getWidth(null));
		} else {
			this.width = width;
			this.height = height;
		}
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		Image image = getImage();
		if (image == null) {
			return;
		}
		ImageObserver io = getImageObserver();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.drawImage(image, x, y, width, height, io == null ? c : io);
	}
}
