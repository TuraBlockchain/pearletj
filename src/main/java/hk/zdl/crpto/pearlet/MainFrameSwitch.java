package hk.zdl.crpto.pearlet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class MainFrameSwitch {

	private final Map<String, Component> map = new TreeMap<>();
	private final Container comp;

	public MainFrameSwitch(Container comp) {
		this.comp = comp;
		comp.setLayout(new BorderLayout());
	}

	public Component put(String key, Component value) {
		return map.put(key, value);
	}

	public void showComponent(String str) {
		Component c = map.get(str);
		Component d = comp.getComponentCount() == 0 ? null : comp.getComponent(0);
		if (c == d) {
			return;
		}
		if (c != null) {
			comp.removeAll();
			comp.add(c, BorderLayout.CENTER);
			comp.revalidate();
			comp.paintAll(comp.getGraphics());
		}
	}

	public Stream<Component> components() {
		return Collections.unmodifiableCollection(map.values()).stream();
	}

}
