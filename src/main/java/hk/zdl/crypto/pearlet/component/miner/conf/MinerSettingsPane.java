package hk.zdl.crypto.pearlet.component.miner.conf;

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
	private final MinerMiscSettingsPane misc_settings_panel = new MinerMiscSettingsPane();
	private final MinerPathSettingPanel miner_path_settings_panel = new MinerPathSettingPanel();
	private final PlotProgressPanel plot_progress_panel = new PlotProgressPanel();
	
	public MinerSettingsPane() {
		super(new GridLayout(2, 2));
		init_panels();
	}

	private void init_panels() {
		add(account_settings_panel);
		add(misc_settings_panel);
		add(miner_path_settings_panel);
		add(plot_progress_panel);
	}

	public void setBasePath(String basePath) {
		account_settings_panel.setBasePath(basePath);
		try {
			account_settings_panel.refresh_list();
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
		}
		misc_settings_panel.setBasePath(basePath);
		try {
			misc_settings_panel.update_server_address();
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
		}
		miner_path_settings_panel.setBasePath(basePath);
		try {
			miner_path_settings_panel.refresh_list();
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
		}
		plot_progress_panel.setBasePath(basePath);
	}

}
