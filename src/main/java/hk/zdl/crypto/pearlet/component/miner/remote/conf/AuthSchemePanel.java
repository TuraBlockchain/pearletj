package hk.zdl.crypto.pearlet.component.miner.remote.conf;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.greenrobot.eventbus.EventBus;
import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.JXLoginPane.Status;
import org.jdesktop.swingx.auth.LoginService;

import hk.zdl.crypto.pearlet.component.miner.remote.ClientUpdateEvent;
import hk.zdl.crypto.pearlet.lock.WalletLock;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class AuthSchemePanel extends JPanel {

	private enum Auth {
		NONE, PASSWORD, PASSPHRASE;

		public String toString() {
			return rsc_bdl.getString("MINING.REMOTE.AUTH.SCHEME." + name());
		}
	}

	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	private static final String miner_auth_path = "/api/v1/miner/configure/auth";
	private static final long serialVersionUID = 7777531371995940084L;
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private JButton method_btn = new JButton(rsc_bdl.getString("MINING.REMOTE.AUTH.CHANGE_SCHEME"));
	private JButton pw_btn = new JButton(rsc_bdl.getString("MINING.REMOTE.AUTH.CHANGE_PASSWORD"));
	private String basePath = "";
	private HttpClient client = HttpClient.newHttpClient();

	public AuthSchemePanel() {
		super(new FlowLayout());
		var panel = new JPanel(new GridBagLayout());
		panel.add(method_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		panel.add(pw_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		add(panel);

		method_btn.addActionListener((e) -> {
			var p = new JPanel(new FlowLayout());
			p.add(new JLabel("Scheme:"));
			var box = new JComboBox<>(Auth.values());
			p.add(box);
			Util.submit(() -> {
				var request = HttpRequest.newBuilder().GET().uri(new URI(basePath + miner_auth_path + "/method")).build();
				var response = client.send(request, BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					box.setSelectedItem(Auth.valueOf(response.body()));
				}
				return null;
			});
			int i = JOptionPane.showConfirmDialog(getRootPane(), p, rsc_bdl.getString("MINING.REMOTE.AUTH.CHOOSE_SCHEME"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (i == JOptionPane.OK_OPTION) {
				Util.submit(() -> {
					var request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(box.getSelectedItem().toString())).uri(new URI(basePath + miner_auth_path + "/method")).build();
					var response = client.send(request, BodyHandlers.discarding());
					if (response.statusCode() == 200) {
						try {
							response = client.send(request, BodyHandlers.discarding());
						} catch (Exception x) {
							method_btn.setEnabled(false);
							JXLoginPane.showLoginDialog(getRootPane(), getLoginService());
						}
					}
					return null;
				});
			}
		});

		pw_btn.addActionListener((e) -> {
			var future = Util.submit(() -> {
				var request = HttpRequest.newBuilder().GET().uri(new URI(basePath + miner_auth_path + "/method")).build();
				var response = client.send(request, BodyHandlers.discarding());
				return response.headers().allValues("WWW-Authenticate").contains("Basic");
			});
			Util.submit(() -> {
				if (future.get()) {
					var status = JXLoginPane.showLoginDialog(getRootPane(), getLoginService());
					if (status != Status.SUCCEEDED) {
						return null;
					}
				}
				var pw_arr = new JPasswordField[] { new JPasswordField(), new JPasswordField() };
				if (UIUtil.show_password_dialog(rsc_bdl.getString("MINING.REMOTE.AUTH.ENTER_PASSWORD"), getRootPane(), pw_arr[0])) {
					if (pw_arr[0].getPassword().length < WalletLock.MIN_PW_LEN) {
						JOptionPane.showMessageDialog(getRootPane(), MessageFormat.format(rsc_bdl.getString("MINING.REMOTE.AUTH.MSG.ERR.MIN_LENGTH"), WalletLock.MIN_PW_LEN), null,
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
					if (UIUtil.show_password_dialog(rsc_bdl.getString("MINING.REMOTE.AUTH.ENTER_PASSWORD_AGAIN"), getRootPane(), pw_arr[1])) {
						if (!Arrays.equals(pw_arr[0].getPassword(), pw_arr[1].getPassword())) {
							JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("MINING.REMOTE.AUTH.MSG.ERR.MIS_MATCH"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
							return null;
						} else {
							var request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(new String(pw_arr[0].getPassword()))).uri(new URI(basePath + miner_auth_path + "/password")).build();
							var response = client.send(request, BodyHandlers.discarding());
							if (response.statusCode() == 200) {
								var client = HttpClient.newBuilder().authenticator(new Authenticator() {
									@Override
									protected PasswordAuthentication getPasswordAuthentication() {
										return new PasswordAuthentication("", pw_arr[0].getPassword());
									}
								}).build();
								EventBus.getDefault().post(new ClientUpdateEvent(basePath, client));
								JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("MINING.REMOTE.AUTH.MSG.DONE"), "", JOptionPane.INFORMATION_MESSAGE);
							}
						}
					}
				}

				return null;
			});
		});
	}

	private LoginService getLoginService() {
		return new LoginService() {

			@Override
			public boolean authenticate(String name, char[] password, String server) throws Exception {
				var client = HttpClient.newBuilder().authenticator(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(name, password);
					}
				}).build();
				var request = HttpRequest.newBuilder().GET().uri(new URI(basePath + miner_auth_path + "/method")).build();

				var response = client.send(request, BodyHandlers.discarding());
				if (response.statusCode() == 200) {
					method_btn.setEnabled(true);
					EventBus.getDefault().post(new ClientUpdateEvent(basePath, client));
					return true;
				} else {
					return false;
				}
			}
		};
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

}
