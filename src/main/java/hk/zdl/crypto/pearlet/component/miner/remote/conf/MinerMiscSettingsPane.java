package hk.zdl.crypto.pearlet.component.miner.remote.conf;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import hk.zdl.crypto.pearlet.component.miner.remote.MinerGridTitleFont;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.WebUtil;

public class MinerMiscSettingsPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4764477299442830256L;
	public static final String miner_conf_serv_u_path = "/api/v1/miner/configure/server_url";
	public static final String miner_reboot_path = "/api/v1/miner/restart";
	private final JTextField server_url_field = new JTextField();
	private final JButton update_serv_url_btn = new JButton("Update");
	private String basePath = "";

	public MinerMiscSettingsPane() {
		super(new GridBagLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Server URL", TitledBorder.CENTER, TitledBorder.TOP, MinerGridTitleFont.getFont()));
		add(server_url_field, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		add(update_serv_url_btn, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		update_serv_url_btn.addActionListener(e -> {
			try {
				var httpclient = WebUtil.getHttpclient();
				var httpPost = new HttpPost(basePath + miner_conf_serv_u_path);
				httpPost.setEntity(new StringEntity(server_url_field.getText().trim()));
				var response = httpclient.execute(httpPost);
				response.getEntity().getContent().close();
				if (response.getStatusLine().getStatusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Set server URL Done!", null);
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
		var line = IOUtils.readLines(new URL(basePath + miner_conf_serv_u_path).openStream(), Charset.defaultCharset()).stream().findFirst().orElseGet(() -> "");
		server_url_field.setText(line);
		if (line.isBlank()) {
			MyDb.get_server_url(CrptoNetworks.ROTURA).ifPresent(s -> {
				server_url_field.setText(s);
				update_serv_url_btn.doClick();
			});
		}
	}
}
