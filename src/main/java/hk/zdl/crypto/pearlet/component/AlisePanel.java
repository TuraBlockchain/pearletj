package hk.zdl.crypto.pearlet.component;

import javax.swing.JTabbedPane;

import hk.zdl.crypto.pearlet.util.Util;

public class AlisePanel extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8795095968649207413L;

	public AlisePanel() {
		var rsc_bdl = Util.getResourceBundle();
		add(new AliseSearchPanel(),rsc_bdl.getString("ALISE_PANEL_SEARCH"));
		add(new AliseRegisterPanel(),rsc_bdl.getString("ALISE_PANEL_REG"));
	}

}
