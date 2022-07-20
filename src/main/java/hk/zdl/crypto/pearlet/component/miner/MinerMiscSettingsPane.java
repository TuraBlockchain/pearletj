package hk.zdl.crypto.pearlet.component.miner;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import hk.zdl.crypto.pearlet.ui.UIUtil;

public class MinerMiscSettingsPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4764477299442830256L;
	public static final String miner_conf_serv_u_path = "/api/v1/miner/configure/server_url";
	public static final String miner_reboot_path = "/api/v1/miner/restart";
	private final JTextField server_url_field = new JTextField(30);
	private final JButton update_serv_url_btn = new JButton("Update");
	private final JButton reboot_miner_btn = new JButton("Reboot Miner");
	private String basePath = "";

	public MinerMiscSettingsPane() {
		super(new GridBagLayout());
		var label_1 = new JLabel("Server URL:");
		add(label_1, new GridBagConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(server_url_field, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(update_serv_url_btn, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		var label_2 = new JLabel("Restart Miner");
		add(label_2, new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(reboot_miner_btn, new GridBagConstraints(0, 3, 2, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		update_serv_url_btn.addActionListener(e -> {
			try {
				var httpclient = HttpClients.createDefault();
				var httpPost = new HttpPost(basePath + miner_conf_serv_u_path);
				httpPost.setEntity(new StringEntity(server_url_field.getText().trim()));
				var response = httpclient.execute(httpPost);
				response.close();
				if (response.getStatusLine().getStatusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Set server URL Done!", null);
				}
			} catch (IOException x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		});
		reboot_miner_btn.addActionListener(e -> {
			try {
				var httpclient = HttpClients.createDefault();
				var httpPost = new HttpPost(basePath + miner_reboot_path);
				var response = httpclient.execute(httpPost);
				response.close();
				if (response.getStatusLine().getStatusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Miner reboot request was sent!", null);
				}
			} catch (IOException x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void update_server_address() throws Exception {
		var line = IOUtils.readLines(new URL(basePath + miner_conf_serv_u_path).openStream(), "UTF-8").get(0);
		server_url_field.setText(line);
	}
}
