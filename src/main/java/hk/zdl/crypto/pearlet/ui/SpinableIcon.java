package hk.zdl.crypto.pearlet.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.Timer;

import org.imgscalr.Scalr;

public class SpinableIcon extends ImageIcon implements ActionListener {

	private static final long serialVersionUID = -366584986274580572L;
	private BufferedImage scaledImage;
	private final int width, height;
	private Timer timer;
	private int fps = 24;
	private int rpm = 60;
	private Component c;
	private double rotationRequired = 0;
	private long last_update = -1;

	public SpinableIcon(Image image, int width, int height) {
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
		if (image instanceof BufferedImage) {
			scaledImage = (BufferedImage) image;
		} else {
			scaledImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
			scaledImage.getGraphics().drawImage(image, 0, 0, null);
		}
		scaledImage = Scalr.resize(scaledImage, Scalr.Method.SPEED, Scalr.Mode.FIT_EXACT, this.width, this.height);
		setImage(scaledImage);
	}

	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		if (c != null) {
			this.c = c;
		}
		super.paintIcon(c, g, x, y);
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

	public void start() {
		stop();
		last_update = -1;
		timer = new Timer(1000 / fps, this);
		timer.start();
	}

	public void stop() {
		if (timer != null) {
			timer.stop();
		}
		timer = null;
		actionPerformed(null);
	}

	public void set_fps(int val) {
		if (val > 1000) {
			val = 1000;
		} else if (val < 1) {
			val = 1;
		}
		fps = val;
		if (timer != null && timer.isRunning()) {
			timer.setDelay(1000 / fps);
		}
	}

	public void set_rpm(int val) {
		rpm = val;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		var now = System.currentTimeMillis();
		if (last_update < 0) {
			last_update = now;
			return;
		}
		if (c == null) {
			return;
		}
		var diff = now - last_update;
		rotationRequired += diff * rpm / 60.0 / 1000.0 * 2 * Math.PI;

		double locationX = width / 2;
		double locationY = height / 2;
		AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		var img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		img.getGraphics().drawImage(op.filter(scaledImage, null), 0, 0, null);
		setImage(img);
		if(c instanceof AbstractButton) {
			var btn = (AbstractButton)c;
			if(!btn.isEnabled()) {
				setImage(GrayFilter.createDisabledImage(img));
			}
		}
		c.repaint();
		last_update = now;
	}

}
