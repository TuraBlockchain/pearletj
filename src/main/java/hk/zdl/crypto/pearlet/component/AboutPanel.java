package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.json.JSONArray;
import org.json.JSONTokener;

import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class AboutPanel extends JPanel {

	public AboutPanel() {
		super(new BorderLayout());
		var panel_0 = new JPanel(new GridBagLayout());
		var icon = new JLabel();
		icon.setHorizontalAlignment(SwingConstants.CENTER);
		try {
			icon.setIcon(new MyStretchIcon(ImageIO.read(Util.getResource("app_icon.png")), 128, -1));
		} catch (IOException e) {
		}
		var insets_5 = new Insets(5, 5, 5, 5);
		panel_0.add(icon, new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var appNameLabel = new JLabel(Util.getProp().get("appName"));
		appNameLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
		appNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel_0.add(appNameLabel, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var appVer = Util.getAppVersion();
		panel_0.add(new_label("Version:", false), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		panel_0.add(new_label(appVer, true), new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		panel_0.add(new_label("Build:", false), new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var appBuild = Stream.of(Util.getTime(getClass())).filter(o -> o != null && o != -1).map(o -> {
			var sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.SIMPLIFIED_CHINESE);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			return sdf.format(new Date(o));
		}).findFirst().orElse("from source code");
		panel_0.add(new_label(appBuild, true), new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

		panel_0.add(new_label("Author:", false), new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		panel_0.add(new_label(Util.getProp().get("authorFullName"), true), new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

		panel_0.add(new_label("License:", false), new GridBagConstraints(0, 5, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var lic_label = new_label(Util.getProp().get("license"), true);
		lic_label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panel_0.add(lic_label, new GridBagConstraints(1, 5, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

		var panel_1 = new JPanel(new FlowLayout());
		panel_1.add(panel_0);
		add(panel_1, BorderLayout.NORTH);

		var poweredby_label = new JLabel("Powered By:");
		poweredby_label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
		poweredby_label.setHorizontalAlignment(SwingConstants.LEFT);
		poweredby_label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		var panel_2 = new JPanel(new BorderLayout());
		panel_2.add(poweredby_label, BorderLayout.NORTH);
		add(panel_2, BorderLayout.CENTER);

		var scr = new JScrollPane();
		scr.setBorder(BorderFactory.createEmptyBorder());
		panel_2.add(scr, BorderLayout.CENTER);
		var badge_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
		scr.setViewportView(badge_panel);
		JSONArray jarr = new JSONArray(new JSONTokener(Util.getResourceAsText("badges.json")));
		var len = Util.getProp().getBoolean("show_peth_only") ? 1 : jarr.length();
		for (int i = 0; i < len; i++) {
			try {
				String _icon = jarr.getJSONObject(i).getString("icon");
				String _text = jarr.getJSONObject(i).getString("text");
				var _label = new JLabel(UIUtil.getStretchIcon("icon/" + _icon, -1, 100));
				_label.setToolTipText(_text);
				badge_panel.add(_label);
			} catch (Exception e) {
				continue;
			}
		}
		lic_label.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					try {
						Util.browse(new URI(Util.getProp().get("license_url")));
					} catch (Exception x) {
						Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
					}
				}
			}
		});
	}

	private static final JLabel new_label(String str, boolean to_east) {
		var o = new JLabel(str);
		if (to_east) {
			o.setHorizontalAlignment(SwingConstants.RIGHT);
			o.setHorizontalTextPosition(SwingConstants.RIGHT);
		}
		o.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		return o;
	};
}
