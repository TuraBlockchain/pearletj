package hk.zdl.crypto.pearlet.component.miner.remote.conf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.WebUtil;

public class MinerServerAddressSettingsPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4764477299442830256L;
	public static final String miner_conf_serv_u_path = "/api/v1/miner/configure/server_url";
	private String basePath = "";
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private final JList<JSONObject> my_list = new JList<>();
	private final JButton add_btn = new JButton("Add");
	private final JButton edit_btn = new JButton("Edit");
	private final JButton del_btn = new JButton("Delete");

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
				} else {
					new URL(url);
				}
				var httpclient = WebUtil.getHttpclient();
				var httpPost = new HttpPost(basePath + miner_conf_serv_u_path);
				httpPost.setEntity(new StringEntity(url));
				var response = httpclient.execute(httpPost);
				response.getEntity().getContent().close();
				if (response.getStatusLine().getStatusCode() == 200) {
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
				} else {
					new URL(url);
				}
				var httpclient = WebUtil.getHttpclient();
				var httpPost = new HttpPut(basePath + miner_conf_serv_u_path);
				var l = new LinkedList<NameValuePair>();
				l.add(new BasicNameValuePair("id", "" + id));
				l.add(new BasicNameValuePair("url", url));
				httpPost.setEntity(new UrlEncodedFormEntity(l));
				var response = httpclient.execute(httpPost);
				response.getEntity().getContent().close();
				if (response.getStatusLine().getStatusCode() == 201) {
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
				var httpclient = WebUtil.getHttpclient();
				var httpPost = new HttpDelete(basePath + miner_conf_serv_u_path + "?id=" + id);
				var response = httpclient.execute(httpPost);
				if (response.getStatusLine().getStatusCode() == 204) {
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

	public void update_server_address() throws Exception {
		if (basePath.isBlank()) {
			return;
		}
		IOUtils.readLines(new URL(basePath + miner_conf_serv_u_path).openStream(), Charset.defaultCharset()).stream().findAny().ifPresent(s -> {
			if (s.startsWith("[")) {
				var jarr = new JSONArray(s);
				var _arr = new JSONObject[jarr.length()];
				for (var i = 0; i < jarr.length(); i++) {
					_arr[i] = jarr.getJSONObject(i);
				}
				var i = my_list.getSelectedIndex();
				my_list.setListData(_arr);
				my_list.setSelectedIndex(i);
			} else {
				var o = new JSONObject();
				o.put("ID", -1);
				o.put("URL", s);
				var i = my_list.getSelectedIndex();
				my_list.setListData(new JSONObject[] { o });
				my_list.setSelectedIndex(i);
			}
		});
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
				value = jobj.getString("URL");
			}
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}

	}
}
