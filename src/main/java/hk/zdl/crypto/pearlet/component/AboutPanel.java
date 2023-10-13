package hk.zdl.crypto.pearlet.component;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
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
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.json.JSONArray;
import org.json.JSONTokener;

import com.jthemedetecor.OsThemeDetector;

import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class AboutPanel extends JPanel {
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);

	public AboutPanel() {
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		var sw_info = new JPanel(new GridBagLayout());
		var icon = new JLabel();
		icon.setHorizontalAlignment(SwingConstants.CENTER);
		try {
			icon.setIcon(new MyStretchIcon(ImageIO.read(Util.getResource("app_icon.png")), 128, -1));
		} catch (IOException e) {
		}
		sw_info.add(icon, new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.NONE, insets_5, 0, 0));
		var appNameLabel = new JLabel(Util.getProp().get("appName"));
		appNameLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
		appNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sw_info.add(appNameLabel, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var appVer = Util.getAppVersion();
		sw_info.add(new_label("Version:", false), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		sw_info.add(new_label(appVer, true), new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		sw_info.add(new_label("Build:", false), new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var appBuild = Stream.of(Util.getTime(getClass())).filter(o -> o != null && o != -1).map(o -> {
			var sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.SIMPLIFIED_CHINESE);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			return sdf.format(new Date(o));
		}).findFirst().orElse("from source code");
		sw_info.add(new_label(appBuild, true), new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		sw_info.add(new_label("Author:", false), new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		sw_info.add(new_label(Util.getProp().get("authorFullName"), true), new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

		sw_info.add(new_label("License:", false), new GridBagConstraints(0, 5, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var lic_label = new_label(Util.getProp().get("license"), true);
		lic_label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		sw_info.add(lic_label, new GridBagConstraints(1, 5, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
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
		sw_info.setMaximumSize(new Dimension(300,400));
		add(sw_info, Component.CENTER_ALIGNMENT);

		var dec_pane = new JTextArea();
		dec_pane.setText(Util.getResourceAsText("disclaimer.txt"));
		dec_pane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
		dec_pane.setEditable(false);
		dec_pane.setWrapStyleWord(true);
		dec_pane.setLineWrap(true);
		set_titled_border(dec_pane, "Disclaimer:");
		dec_pane.setMinimumSize(new Dimension(600,300));
		add(dec_pane, Component.LEFT_ALIGNMENT);

		var pwby_pane = new JScrollPane();
		pwby_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		pwby_pane.setBorder(BorderFactory.createEmptyBorder());
		pwby_pane.setViewportView(create_badge_panel());
		set_titled_border(pwby_pane, "Powered By:");
		add(pwby_pane, Component.LEFT_ALIGNMENT);

	}

	private static final JPanel create_badge_panel() {
		var badge_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
		var jarr = new JSONArray(new JSONTokener(Util.getResourceAsText("badges.json")));
		for (int i = 0; i < jarr.length(); i++) {
			try {
				var _icon_dark = jarr.getJSONObject(i).optString("icon_dark");
				var _icon = jarr.getJSONObject(i).getString("icon");
				var _text = jarr.getJSONObject(i).getString("text");
				var icon = UIUtil.getStretchIcon("icon/" + _icon, -1, 100);
				var _label = new JLabel(icon);
				_label.setToolTipText(_text);
				badge_panel.add(_label);
				if (!_icon_dark.isBlank()) {
					var dark_icon = UIUtil.getStretchIcon("icon/" + _icon_dark, -1, 100);
					OsThemeDetector.getDetector().registerListener((isDark) -> {
						_label.setIcon(isDark ? dark_icon : icon);
					});
					if (OsThemeDetector.getDetector().isDark()) {
						_label.setIcon(dark_icon);
					}
				}
			} catch (Exception e) {
				continue;
			}
		}
		return badge_panel;
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

	private static final void set_titled_border(JComponent c, String title) {
		c.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), title, TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, new Font(Font.MONOSPACED, Font.BOLD, 20)));
	}
}