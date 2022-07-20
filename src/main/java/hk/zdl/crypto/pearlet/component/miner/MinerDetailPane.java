package hk.zdl.crypto.pearlet.component.miner;

import javax.swing.JTabbedPane;

import org.json.JSONObject;

import hk.zdl.crypto.pearlet.component.miner.conf.MinerSettingsPane;

public class MinerDetailPane extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6324183845145603011L;
	public static final String miner_status_path = "/api/v1/status";

	private final StatusPane status_pane = new StatusPane();
	private final MinerSettingsPane settings_pane = new MinerSettingsPane();

	public MinerDetailPane() {
		add("Status", status_pane);
		add("Configure", settings_pane);
	}

	public void setBasePath(String basePath) {
		status_pane.setBasePath(basePath);
		settings_pane.setBasePath(basePath);
	}

	public void setStatus(JSONObject status) {
		status_pane.setStatus(status);
	}

}
