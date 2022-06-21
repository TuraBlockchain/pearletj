package hk.zdl.crpto.pearlet;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.json.JSONArray;
import org.json.JSONTokener;

import hk.zdl.crpto.pearlet.component.MyStretchIcon;

@SuppressWarnings("serial")
public class MyToolbar extends JScrollPane {
	private final JPanel panel = new JPanel(new GridLayout(0, 1));

	public MyToolbar() {
		init();
	}

	private void init() {
		setViewportView(panel);
		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		init_buttons();
	}

	private void init_buttons() {
		JSONArray jarr = new JSONArray(new JSONTokener(getClass().getClassLoader().getResourceAsStream("toolbar.json")));
		for (int i = 0; i < jarr.length(); i++) {
			String text = jarr.getJSONObject(i).getString("text");
			String icon = jarr.getJSONObject(i).getString("icon");
			var btn = new JToggleButton(text, getIcon(icon));
			btn.setHorizontalAlignment(SwingConstants.LEFT);
			btn.setPreferredSize(new Dimension(200, 50));
			panel.add(btn);
		}

	}

	private final Icon getIcon(String str) {
		try {
			return new MyStretchIcon(ImageIO.read(MyToolbar.class.getClassLoader().getResource("toolbar/" + str)),32,32);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
