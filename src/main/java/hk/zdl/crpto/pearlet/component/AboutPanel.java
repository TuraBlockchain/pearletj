package hk.zdl.crpto.pearlet.component;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import hk.zdl.crpto.pearlet.Main;
import hk.zdl.crpto.pearlet.MyToolbar;
import hk.zdl.crpto.pearlet.util.Util;

@SuppressWarnings("serial")
public class AboutPanel extends JPanel {

	public AboutPanel() {
		super(new FlowLayout());
		var panel = new JPanel(new GridBagLayout());
		var icon = new JLabel();
		try {
			icon.setIcon(new MyStretchIcon(ImageIO.read(MyToolbar.class.getClassLoader().getResource("app_icon.png")), 128, 128));
		} catch (IOException e) {
		}
		panel.add(icon, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
		var appNameLabel = new JLabel(Util.getProp().get("appName"));
		appNameLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
		panel.add(appNameLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
		var appVer = Main.class.getPackage().getImplementationVersion();
		if (appVer == null) {
			appVer = Util.getProp().get("appVersion");
		}
		var appVersionLabel = new JLabel("Version: " + appVer);
		appVersionLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		panel.add(appVersionLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
		var authorNameLabel = new JLabel("Author: " + Util.getProp().get("appAuthor"));
		authorNameLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		panel.add(authorNameLabel, new GridBagConstraints(0, 3, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));

		add(panel);
	}

}
