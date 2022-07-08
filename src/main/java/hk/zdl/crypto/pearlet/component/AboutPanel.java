package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class AboutPanel extends JPanel {

	public AboutPanel() {
		super(new BorderLayout());
		var panel_0 = new JPanel(new GridBagLayout());
		var icon = new JLabel();
		try {
			icon.setIcon(new MyStretchIcon(ImageIO.read(Util.getResource("app_icon.png")), 128, -1));
		} catch (IOException e) {
		}
		var insets_5 = new Insets(5, 5, 5, 5);
		panel_0.add(icon, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var appNameLabel = new JLabel(Util.getProp().get("appName"));
		appNameLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
		panel_0.add(appNameLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var appVer = AboutPanel.class.getPackage().getImplementationVersion();
		if (appVer == null) {
			appVer = Util.getProp().get("appVersion");
		}
		var appVersionLabel = new JLabel("Version: " + appVer);
		appVersionLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		panel_0.add(appVersionLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var authorNameLabel = new JLabel("Author: " + Util.getProp().get("authorFullName"));
		authorNameLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		panel_0.add(authorNameLabel, new GridBagConstraints(0, 3, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));

		add(panel_0, BorderLayout.NORTH);

		var poweredby_label = new JLabel("Powered By:");
		poweredby_label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
		poweredby_label.setHorizontalAlignment(SwingConstants.LEFT);
		poweredby_label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		var panel_1 = new JPanel(new BorderLayout());
		panel_1.add(poweredby_label, BorderLayout.NORTH);
		add(panel_1, BorderLayout.CENTER);

		var scr = new JScrollPane();
		scr.setBorder(BorderFactory.createEmptyBorder());
		panel_1.add(scr, BorderLayout.CENTER);

		var badge_panel = new JPanel(new FlowLayout(FlowLayout.LEFT,20,5));
		scr.setViewportView(badge_panel);

		try {
			var x = new JLabel(new MyStretchIcon(ImageIO.read(Util.getResource("icon/" + "Signum_Badge_J.png")), -1, 100));
			x.setToolTipText("SignumJ");
			badge_panel.add(x);
		} catch (IOException e) {
		}
		try {
			var x = new JLabel(new MyStretchIcon(ImageIO.read(Util.getResource("icon/" + "web3j_logo.png")), -1, 100));
			x.setToolTipText("Web3j");
			badge_panel.add(x);
		} catch (IOException e) {
		}


	}
}
