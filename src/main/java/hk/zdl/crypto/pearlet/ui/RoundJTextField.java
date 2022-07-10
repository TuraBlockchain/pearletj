package hk.zdl.crypto.pearlet.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JTextField;

public class RoundJTextField extends JTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7376368709483829325L;
	private Shape shape;

	public RoundJTextField(int size) {
		super(size);
		setOpaque(false);
	}

	protected void paintComponent(Graphics g) {
		Color c = getBackground();
		g.setColor(c);
		g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight() - 1, getHeight() - 1);
		setBackground(new Color(0,0,0,0));//Workacound
		super.paintComponent(g);
		setBackground(c);
	}

	protected void paintBorder(Graphics g) {
		g.setColor(getForeground());
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight() - 1, getHeight() - 1);
	}

	public boolean contains(int x, int y) {
		if (shape == null || !shape.getBounds().equals(getBounds())) {
			shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, getHeight() - 1, getHeight() - 1);
		}
		return shape.contains(x, y);
	}
}