package hk.zdl.crpto.pearlet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.Map;
import java.util.TreeMap;

public class MainFrameSwitch{

	private final Map<String,Component> map = new TreeMap<>();
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
		if(c!=null) {
			comp.removeAll();
			comp.add(c,BorderLayout.CENTER);
			comp.revalidate();
		}
	}
}
