package hk.zdl.crypto.pearlet;

import java.awt.Dimension;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;

import com.formdev.flatlaf.extras.FlatDesktop;

import hk.zdl.crypto.pearlet.component.event.SettingsPanelEvent;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class MyToolbar extends JToolBar {
	private final MainFrameSwitch mfs;
	private final Map<String, JToggleButton> buttons = new TreeMap<>();

	public MyToolbar(MainFrameSwitch mfs) {
		super(SwingConstants.VERTICAL);
		this.mfs = mfs;
		init_buttons();
		set_callbacks();
		EventBus.getDefault().register(this);
	}

	private void init_buttons() {
		var btn_gp = new ButtonGroup();
		JSONArray jarr = new JSONArray(Util.getResourceAsText("toolbar.json"));
		for (int i = 0; i < jarr.length(); i++) {
			String id = jarr.getJSONObject(i).getString("id");
			String text = jarr.getJSONObject(i).getString("text");
			String icon = jarr.getJSONObject(i).getString("icon");
			var btn = new JToggleButton(text, getIcon(icon)) {

				@Override
				public Dimension getMaximumSize() {
					var h = super.getMaximumSize().getHeight();
					return new Dimension(Short.MAX_VALUE, (int) h);
				}};
			btn.setBorderPainted(false);
			btn.setFocusPainted(false);
			btn.setFocusable(true);
			btn.addActionListener((e) -> mfs.showComponent(buttons.entrySet().stream().filter(x -> x.getValue() == e.getSource()).findAny().get().getKey()));
			btn.setHorizontalAlignment(SwingConstants.LEFT);
			btn.setMultiClickThreshhold(300);
			buttons.put(id, btn);
			btn_gp.add(btn);
			add(btn);
		}
	}

	private void set_callbacks() {
		FlatDesktop.setAboutHandler(() -> clickButton("about"));
		FlatDesktop.setPreferencesHandler(() -> clickButton("sets"));
	}

	public void clickButton(String str) {
		Optional.ofNullable(buttons.get(str)).ifPresent(JToggleButton::doClick);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(SettingsPanelEvent e) {
		clickButton("sets");
	}

	private static final Icon getIcon(String str) {
		return UIUtil.getStretchIcon("toolbar/" + str, 32, 32);
	}
}
