package hk.zdl.crpto.pearlet.component;

import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class AccountInfoPanel extends JTabbedPane {

	public AccountInfoPanel() {
		addTab("Copy", new CopyAccountInfoPanel());
		addTab("Set", new SetAccountInfoPanel());
	}

}
