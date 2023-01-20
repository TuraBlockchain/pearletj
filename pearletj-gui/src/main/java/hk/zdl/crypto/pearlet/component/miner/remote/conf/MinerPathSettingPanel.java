package hk.zdl.crypto.pearlet.component.miner.remote.conf;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import hk.zdl.crypto.pearlet.component.miner.remote.MinerGridTitleFont;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class MinerPathSettingPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -718519273950546176L;
	public static final String miner_file_path = "/api/v1/miner_path";
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);

	private final JList<String> path_list = new JList<>();
	private final JButton add_btn = new JButton("Add");
	private final JButton del_btn = new JButton("Del");
	private String id = "";

	private String basePath = "";

	public MinerPathSettingPanel() {
		super(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Miner Path", TitledBorder.CENTER, TitledBorder.TOP, MinerGridTitleFont.getFont()));
		add(new JScrollPane(path_list), BorderLayout.CENTER);
		var btn_panel = new JPanel(new GridBagLayout());
		btn_panel.add(add_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		btn_panel.add(del_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));

		var panel_1 = new JPanel(new FlowLayout(1, 0, 0));
		panel_1.add(btn_panel);
		add(panel_1, BorderLayout.EAST);

		add_btn.addActionListener(e -> Util.submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				if (add_miner_path()) {
					refresh_list();
				}
				return null;
			}
		}));
		del_btn.addActionListener(e -> Util.submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				if (del_miner_path()) {
					refresh_list();
				}
				return null;
			}
		}));

	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void setId(String id) {
		this.id = id;
	}

	@SuppressWarnings("unchecked")
	public void refresh_list() throws Exception {
		var l = new JSONArray(new JSONTokener(new URL(basePath + miner_file_path + "/list?id=" + id).openStream())).toList().stream().map(o -> o.toString()).toList();
		path_list.setModel(new ListComboBoxModel<String>(l));
	}

	public boolean add_miner_path() {
		var icon = UIUtil.getStretchIcon("icon/" + "signpost-2.svg", 64, 64);
		var txt_field = new JTextField(30);
		int i = JOptionPane.showConfirmDialog(getRootPane(), txt_field, "Please Enter path for plot files:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
		if (i == JOptionPane.OK_OPTION) {
			var path = txt_field.getText().trim();
			if (path.isBlank()) {
				JOptionPane.showMessageDialog(getRootPane(), "Path cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				try {
					var httpPost = new HttpPost(basePath + miner_file_path + "/add");
					var jobj = new JSONObject();
					jobj.put("id", id);
					jobj.put("path", path);
					httpPost.setEntity(new StringEntity(jobj.toString()));
					httpPost.setHeader("Content-type", "application/json");
					var httpclient = HttpClients.createDefault();
					var response = httpclient.execute(httpPost);
					if (response.getStatusLine().getStatusCode() != 200) {
						var text = IOUtils.readLines(response.getEntity().getContent(), Charset.defaultCharset()).get(0);
						response.close();
						throw new IllegalArgumentException(text);
					}
				} catch (Exception x) {
					JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
					return false;
				}

			}
		}
		return true;
	}

	public boolean del_miner_path() {
		if (path_list.getSelectedIndex() < 0) {
			return false;
		}
		int i = JOptionPane.showConfirmDialog(getRootPane(), "Are you sure to delete it?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == JOptionPane.YES_OPTION) {
			try {
				var httpPost = new HttpPost(basePath + miner_file_path + "/del");
				var jobj = new JSONObject();
				jobj.put("id", id);
				jobj.put("path", path_list.getSelectedValue());
				httpPost.setEntity(new StringEntity(jobj.toString()));
				httpPost.setHeader("Content-type", "application/json");
				var httpclient = HttpClients.createDefault();
				var response = httpclient.execute(httpPost);
				if (response.getStatusLine().getStatusCode() != 200) {
					var text = IOUtils.readLines(response.getEntity().getContent(), Charset.defaultCharset()).get(0);
					response.close();
					throw new IllegalArgumentException(text);
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}
}
