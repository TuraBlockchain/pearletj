package hk.zdl.crypto.pearlet.component.miner;

import javax.swing.JTabbedPane;

import org.json.JSONObject;

public class MinerDetailPane extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6324183845145603011L;
	public static final String miner_status_path = "/api/v1/status";
	private String basePath = "";

	private final StatusPane status_pane = new StatusPane();

	public MinerDetailPane() {
		add("Status", status_pane);
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void setStatus(JSONObject status) {
		status_pane.setStatus(status);
	}

}
