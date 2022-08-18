package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class UnderConstruction extends JPanel {

	private static final long serialVersionUID = -6312581986095019782L;
	private BufferedImage img = new BufferedImage(1,1,1);

	public UnderConstruction() {
		super(new BorderLayout());
		try {
			img = ImageIO.read(getClass().getClassLoader().getResource("SL-043020-30500-40.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void paint(Graphics g) {
		g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
	}

}
