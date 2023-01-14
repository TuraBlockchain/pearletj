package hk.zdl.crypto.pearlet.component.miner.remote.conf;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.json.JSONArray;
import org.json.JSONTokener;

import hk.zdl.crypto.pearlet.component.miner.remote.MinerGridTitleFont;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import signumj.crypto.SignumCrypto;

public class MinerAccountSettingsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1698208979389357636L;
	public static final String miner_account_path = "/api/v1/miner/configure/account";
	private final JComboBox<String> acc_cbox = new JComboBox<String>();
	private final JButton add_btn = new JButton("Add");
	private final JButton del_btn = new JButton("Del");
	private String basePath = "";

	public MinerAccountSettingsPanel() {
		super(new GridBagLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Miner Account", TitledBorder.CENTER, TitledBorder.TOP, MinerGridTitleFont.getFont()));
		add(acc_cbox, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, 1, new Insets(5, 5, 5, 5), 0, 0));
		add(add_btn, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(5, 5, 5, 5), 0, 0));
		add(del_btn, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 0, new Insets(5, 5, 5, 5), 0, 0));
		add_btn.addActionListener(e -> {
			if (add_account()) {
				try {
					refresh_list();
				} catch (Exception x) {
				}
			}
		});
		del_btn.addActionListener(e -> {
			if (del_account()) {
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
		var l = new JSONArray(new JSONTokener(new URL(basePath + miner_account_path).openStream())).toList().stream().map(o -> o.toString()).toList();
		acc_cbox.setModel(new ListComboBoxModel<String>(l));
	}

	public boolean add_account() {
		var icon = UIUtil.getStretchIcon("icon/" + "wallet_2.svg", 64, 64);
		var txt_field = new JTextField(30);
		int i = JOptionPane.showConfirmDialog(getRootPane(), txt_field, "Please Enter your Passphase", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
		if (i == JOptionPane.OK_OPTION) {
			var phase = txt_field.getText().trim();
			if (phase.isBlank()) {
				JOptionPane.showMessageDialog(getRootPane(), "Passphrase cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				try {
					var id = SignumCrypto.getInstance().getAddressFromPassphrase(phase).getID();
					var client = new OkHttpClient();
					var request = new Request.Builder().url(basePath + miner_account_path + "/add").post(new FormBody(Arrays.asList("id", "passphrase"), Arrays.asList(id, phase))).build();
					var response = client.newCall(request).execute();
					if (!response.isSuccessful()) {
						throw new Exception(response.body().string());
					}
				} catch (Exception x) {
					JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
					return false;
				}

			}
		}
		return true;
	}

	public boolean del_account() {
		if (acc_cbox.getSelectedIndex() < 0) {
			return false;
		}
		int i = JOptionPane.showConfirmDialog(getRootPane(), "Are you sure to delete this account?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == JOptionPane.YES_OPTION) {
			try {
				var id = acc_cbox.getSelectedItem().toString();
				var client = new OkHttpClient();
				var request = new Request.Builder().url(basePath + miner_account_path + "/del").post(new FormBody(Arrays.asList("id"), Arrays.asList(id))).build();
				var response = client.newCall(request).execute();
				if (!response.isSuccessful()) {
					throw new Exception(response.body().string());
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}
}
