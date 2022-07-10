package hk.zdl.crypto.pearlet.component;

import javax.swing.JTabbedPane;

public class AlisesPanel extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8795095968649207413L;

	public AlisesPanel() {
		add(new AlisesSearchPanel(),"Search");
		add(new AlisesRegisterPanel(),"Register");
	}

}
