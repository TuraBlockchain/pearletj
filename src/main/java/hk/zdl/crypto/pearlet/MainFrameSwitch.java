package hk.zdl.crypto.pearlet;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;

public class MainFrameSwitch {

	private final CardLayout layout = new CardLayout();
	private final Container comp;

	public MainFrameSwitch(Container comp) {
		this.comp = comp;
		comp.setLayout(layout);
	}

	public void put(String key, Component value) {
		comp.add(value, key);
	}

	public void showComponent(String str) {
		layout.show(comp, str);
	}

}
