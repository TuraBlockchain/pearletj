package hk.zdl.crypto.pearlet.component;

import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class AccountInfoPanel extends JTabbedPane {

	public AccountInfoPanel() {
		var panel = new JPanel(new FlowLayout());
		panel.add(new SetAccountInfoPanel());
		addTab("Copy", new CopyAccountInfoPanel());
		addTab("Set", panel);
	}

}
