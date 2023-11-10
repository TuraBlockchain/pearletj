package hk.zdl.crypto.pearlet.component;

import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class AccountInfoPanel extends JTabbedPane {

	public AccountInfoPanel() {
		var rsc_bdl = Util.getResourceBundle();
		var panel = new JPanel(new FlowLayout());
		panel.add(new SetAccountInfoPanel());
		addTab(rsc_bdl.getString("ACCOUNT_INFO_PANEL.COPY"), new CopyAccountInfoPanel());
		addTab(rsc_bdl.getString("ACCOUNT_INFO_PANEL.SET"), panel);
	}

}
