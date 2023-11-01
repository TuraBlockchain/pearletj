package hk.zdl.crypto.pearlet.component.miner.remote.conf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.json.JSONArray;
import org.json.JSONObject;

import hk.zdl.crypto.pearlet.ui.UIUtil;

public class MinerServerAddressSettingsPane extends JPanel {

	private static final String miner_conf_serv_u_path = "/api/v1/miner/configure/server_url";
	private static final long serialVersionUID = 4764477299442830256L;
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private final JList<JSONObject> my_list = new JList<>();
	private final JButton add_btn = new JButton("Add");
	private final JButton edit_btn = new JButton("Edit");
	private final JButton del_btn = new JButton("Delete");
	private HttpClient client = HttpClient.newHttpClient();
	private String basePath = "";

	public MinerServerAddressSettingsPane() {
		super(new BorderLayout());
		my_list.setCellRenderer(new MyCellRenderer());
		add(new JScrollPane(my_list), BorderLayout.CENTER);
		var btn_panel = new JPanel(new GridBagLayout());
		btn_panel.add(add_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(edit_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(del_btn, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

		var panel_1 = new JPanel(new FlowLayout(1, 0, 0));
		panel_1.add(btn_panel);
		add(panel_1, BorderLayout.EAST);
		add_btn.addActionListener(e -> {
			try {
				var url = String.valueOf(JOptionPane.showInputDialog(getRootPane(), "Please input URL of node:")).trim();
				if ("null".equals(String.valueOf(url))) {
					return;
				} else if (url.isBlank()) {
					JOptionPane.showMessageDialog(getRootPane(), "URL cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				var request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(url, Charset.defaultCharset())).uri(new URI(basePath + miner_conf_serv_u_path)).build();
				var response = client.send(request, BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Set server URL Done!");
					update_server_address();
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		});
		edit_btn.addActionListener(e -> {
			if (my_list.getSelectedIndex() < 0) {
				return;
			}
			try {
				int id = my_list.getSelectedValue().getInt("ID");
				var old_val = my_list.getSelectedValue().getString("URL");
				var url = String.valueOf(JOptionPane.showInputDialog(getRootPane(), "Please input URL of node:", old_val)).trim();
				if ("null".equals(String.valueOf(url))) {
					return;
				} else if (url.isBlank()) {
					JOptionPane.showMessageDialog(getRootPane(), "URL cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				var jobj = new JSONObject();
				jobj.put("id", id);
				jobj.put("url", url);
				var request = HttpRequest.newBuilder().PUT(BodyPublishers.ofString(jobj.toString(), Charset.defaultCharset())).uri(new URI(basePath + miner_conf_serv_u_path))
						.header("Content-type", "application/json").build();
				var response = client.send(request, BodyHandlers.ofString());
				if (response.statusCode() == 201) {
					UIUtil.displayMessage("Succeed", "Set server URL Done!");
					update_server_address();
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		});
		del_btn.addActionListener(e -> {
			if (my_list.getSelectedIndex() < 0) {
				return;
			}
			try {
				int id = my_list.getSelectedValue().getInt("ID");
				var i = JOptionPane.showConfirmDialog(getRootPane(), "Are you sure to delete this?", null, JOptionPane.YES_NO_OPTION);
				if (i != JOptionPane.YES_OPTION) {
					return;
				}
				var request = HttpRequest.newBuilder().DELETE().uri(new URI(basePath + miner_conf_serv_u_path + "?id=" + id)).build();
				var response = client.send(request, BodyHandlers.ofString());
				if (response.statusCode() == 204) {
					UIUtil.displayMessage("Succeed", "Set server URL Deleted!");
					update_server_address();
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		});

		my_list.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					del_btn.doClick();
				}
			}
		});
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	public void update_server_address() throws Exception {
		if (basePath.isBlank()) {
			return;
		}
		var request = HttpRequest.newBuilder().GET().uri(new URI(basePath + miner_conf_serv_u_path)).build();
		var response = client.send(request, BodyHandlers.ofString());
		if (response.statusCode() == 200) {
			var jarr = new JSONArray(response.body()); 
			var _arr = new JSONObject[jarr.length()];
			for (var i = 0; i < jarr.length(); i++) {
				_arr[i] = jarr.getJSONObject(i);
			}
			var i = my_list.getSelectedIndex();
			my_list.setListData(_arr);
			my_list.setSelectedIndex(i);
		}
	}

	private static final class MyCellRenderer extends DefaultListCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1788797707547063139L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof JSONObject) {
				var jobj = (JSONObject) value;
				value = jobj.optString("URL");
			}
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}

	}
}
