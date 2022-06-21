package hk.zdl.crpto.pearlet.component;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;

public class MyStretchIcon extends ImageIcon {

	private final int width, height;

	public MyStretchIcon(Image image, int width, int height) {
		super(image);
		this.width = width;
		this.height = height;
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
		g.drawImage(image, x, y, width, height, io == null ? c : io);
	}
}
