package hk.zdl.crypto.pearlet;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class MySplansh extends JFrame{
	
	private static final long serialVersionUID = -1636454413467223885L;
	private final BufferedImage img;

	private MySplansh(BufferedImage img) throws HeadlessException {
		this.img = img;
	}


	public static void main(String[] args) throws Throwable {
		var img = ImageIO.read(MySplansh.class.getResource("/splash.png"));
		var size = new Dimension(img.getWidth(),img.getHeight());
		var frame = new MySplansh(img);
		frame.setAlwaysOnTop(true);
		frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setPreferredSize(size);
		frame.setMinimumSize(size);
		frame.setMaximumSize(size);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		Main.main(args);
		frame.dispose();
	}

	@Override
	public void paint(Graphics g) {
		g.drawImage(img, 0, 0, null);
	}
}
