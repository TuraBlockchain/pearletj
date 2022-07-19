package hk.zdl.crypto.pearlet.component;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.imgscalr.Scalr;

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
		BufferedImage scaledImage;
		if (image instanceof BufferedImage) {
			scaledImage = (BufferedImage) image;
		} else {
			scaledImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
			scaledImage.getGraphics().drawImage(image, 0, 0, null);
		}
		scaledImage = Scalr.resize(scaledImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, this.width, this.height);
		setImage(scaledImage);
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
