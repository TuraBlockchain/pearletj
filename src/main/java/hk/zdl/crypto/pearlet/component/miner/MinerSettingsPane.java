package hk.zdl.crypto.pearlet.component.miner;

import java.awt.GridLayout;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

public class MinerSettingsPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3040618948690123951L;
	private final MinerAccountSettingsPanel account_settings_panel = new MinerAccountSettingsPanel();
	private String basePath = "";
	
	public MinerSettingsPane() {
		super(new GridLayout(2, 2));
		init_panels();
	}

	private void init_panels() {
		add(account_settings_panel);
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
		account_settings_panel.setBasePath(basePath);
		try {
			account_settings_panel.refresh_list();
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
		}
	}

}
