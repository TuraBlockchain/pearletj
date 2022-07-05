package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import hk.zdl.crpto.pearlet.Main;
import hk.zdl.crpto.pearlet.MyToolbar;
import hk.zdl.crpto.pearlet.util.Util;

@SuppressWarnings("serial")
public class AboutPanel extends JPanel {

	public AboutPanel() {
		super(new BorderLayout());
		var panel_0 = new JPanel(new GridBagLayout());
		var icon = new JLabel();
		try {
			icon.setIcon(new MyStretchIcon(ImageIO.read(MyToolbar.class.getClassLoader().getResource("app_icon.png")), 128, 128));
		} catch (IOException e) {
		}
		panel_0.add(icon, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
		var appNameLabel = new JLabel(Util.getProp().get("appName"));
		appNameLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
		panel_0.add(appNameLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
		var appVer = Main.class.getPackage().getImplementationVersion();
		if (appVer == null) {
			appVer = Util.getProp().get("appVersion");
		}
		var appVersionLabel = new JLabel("Version: " + appVer);
		appVersionLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		panel_0.add(appVersionLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
		var authorNameLabel = new JLabel("Author: " + Util.getProp().get("appAuthor"));
		authorNameLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		panel_0.add(authorNameLabel, new GridBagConstraints(0, 3, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));

		add(panel_0, BorderLayout.NORTH);

		var poweredby_label = new JLabel("Powered By:");
		poweredby_label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
		poweredby_label.setHorizontalAlignment(SwingConstants.LEFT);
		poweredby_label.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));

		var panel_1 = new JPanel(new BorderLayout());
		panel_1.add(poweredby_label, BorderLayout.NORTH);
		add(panel_1, BorderLayout.CENTER);

		var scr = new JScrollPane();
		scr.setBorder(BorderFactory.createEmptyBorder());
		panel_1.add(scr, BorderLayout.CENTER);

		var panel_2 = new JPanel(new FlowLayout(FlowLayout.LEFT));

		try {
			BufferedImage img = ImageIO.read(MyToolbar.class.getClassLoader().getResource("icon/" + "Signum_Logo_V1_black.svg"));
			img = img.getSubimage(0, 150, 400, 100);
			var signum_label = new JLabel(new ImageIcon(img));
			panel_2.add(signum_label);
		} catch (IOException e) {
		}

		scr.setViewportView(panel_2);

	}
}
