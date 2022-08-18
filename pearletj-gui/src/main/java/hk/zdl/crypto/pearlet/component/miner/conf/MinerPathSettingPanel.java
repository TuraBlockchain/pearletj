package hk.zdl.crypto.pearlet.component.miner.conf;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.json.JSONArray;
import org.json.JSONTokener;

import hk.zdl.crypto.pearlet.component.miner.MinerGridTitleFont;
import hk.zdl.crypto.pearlet.ui.UIUtil;

public class MinerPathSettingPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -718519273950546176L;
	public static final String miner_file_path = "/api/v1/miner_path";
	private final JComboBox<String> acc_cbox = new JComboBox<String>();
	private final JButton add_btn = new JButton("Add");
	private final JButton del_btn = new JButton("Del");

	private String basePath = "";

	public MinerPathSettingPanel() {
		super(new GridBagLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Miner Path", TitledBorder.CENTER, TitledBorder.TOP, MinerGridTitleFont.getFont()));
		add(acc_cbox, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, 1, new Insets(5, 5, 5, 5), 0, 0));
		add(add_btn, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(5, 5, 5, 5), 0, 0));
		add(del_btn, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(5, 5, 5, 5), 0, 0));
		add_btn.addActionListener(e -> {
			if (add_miner_path()) {
				try {
					refresh_list();
				} catch (Exception x) {
				}
			}
		});
		del_btn.addActionListener(e -> {
			if (del_miner_path()) {
				try {
					refresh_list();
				} catch (Exception x) {
				}
			}
		});
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	@SuppressWarnings("unchecked")
	public void refresh_list() throws Exception {
		var l = new JSONArray(new JSONTokener(new URL(basePath + miner_file_path + "/list").openStream())).toList().stream().map(o -> o.toString()).toList();
		acc_cbox.setModel(new ListComboBoxModel<String>(l));
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
					var httpclient = HttpClients.createDefault();
					var httpPost = new HttpPost(basePath + miner_file_path + "/add");
					httpPost.setEntity(new StringEntity(path));
					var response = httpclient.execute(httpPost);
					response.close();
					if (response.getStatusLine().getStatusCode() == 200) {
						UIUtil.displayMessage("Succeed", "Add miner path succeed!", null);
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
		if (acc_cbox.getSelectedIndex() < 0) {
			return false;
		}
		int i = JOptionPane.showConfirmDialog(getRootPane(), "Are you sure to delete it?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == JOptionPane.YES_OPTION) {
			try {
				var httpclient = HttpClients.createDefault();
				var httpPost = new HttpPost(basePath + miner_file_path + "/del");
				httpPost.setEntity(new StringEntity(acc_cbox.getSelectedItem().toString()));
				var response = httpclient.execute(httpPost);
				response.close();
				if (response.getStatusLine().getStatusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Deleted.", null);
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}
}
