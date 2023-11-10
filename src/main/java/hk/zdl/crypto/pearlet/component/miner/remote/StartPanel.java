package hk.zdl.crypto.pearlet.component.miner.remote;

import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.Authenticator;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.JXLoginPane.Status;
import org.jdesktop.swingx.auth.LoginService;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CaptorTool;
import hk.zdl.crypto.pearlet.util.Util;

final class StartPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6066932317672101910L;
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	private final JComboBox<Inet4Address> iface_combobox = new JComboBox<>();
	private final JSpinner port_spinner = new JSpinner(new SpinnerNumberModel(8080, 1050, 65500, 1));
	private final JButton start_button = new JButton(rsc_bdl.getString("MINER.REMOTE.START.BTN_TEXT"));
	private final JTextField miner_url_field = new JTextField(30);
	private final JButton inspect_button = new JButton(rsc_bdl.getString("MINER.REMOTE.INSP.BTN_TEXT"));
	private final JProgressBar p_bar = new JProgressBar();
	private final MinerExplorePane pane;

	@SuppressWarnings("serial")
	public StartPanel(MinerExplorePane pane) {
		super(new GridBagLayout());
		this.pane = pane;
		var inetface_label = new JLabel(rsc_bdl.getString("MINER.REMOTE.INET_FACE"));
		add(inetface_label, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, 1, new Insets(5, 5, 0, 5), 0, 0));
		add(iface_combobox, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.WEST, 1, new Insets(5, 5, 5, 5), 0, 0));
		var inetport_label = new JLabel(rsc_bdl.getString("MINER.REMOTE.INET_PORT"));
		add(inetport_label, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 1, new Insets(5, 5, 0, 5), 0, 0));
		add(port_spinner, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 1, new Insets(5, 5, 5, 5), 0, 0));
		var start_label = new JLabel(rsc_bdl.getString("MINER.REMOTE.SEARCH"));
		add(start_label, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 1, new Insets(5, 5, 0, 5), 0, 0));
		add(start_button, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 1, new Insets(5, 5, 5, 5), 0, 0));

		var url_label = new JLabel(rsc_bdl.getString("MINER.REMOTE.CHECK_FOR_URL"));
		add(url_label, new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.WEST, 1, new Insets(5, 5, 0, 5), 0, 0));
		add(miner_url_field, new GridBagConstraints(0, 3, 2, 1, 1, 0, GridBagConstraints.WEST, 1, new Insets(5, 5, 5, 5), 0, 0));
		add(inspect_button, new GridBagConstraints(2, 3, 1, 1, 0, 0, GridBagConstraints.CENTER, 1, new Insets(5, 5, 5, 5), 0, 0));

		add(p_bar, new GridBagConstraints(0, 4, 3, 1, 1, 0, GridBagConstraints.CENTER, 1, new Insets(5, 5, 5, 5), 0, 0));

		iface_combobox.setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				if (value == null) {
					return super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
				} else {
					Inet4Address adr = (Inet4Address) value;
					return super.getListCellRendererComponent(list, adr.getHostAddress(), index, isSelected, cellHasFocus);
				}
			}

		});
		addComponentListener(new ComponentAdapter() {

			@Override
			@SuppressWarnings("unchecked")
			public void componentShown(ComponentEvent e) {
				SwingUtilities.invokeLater(() -> {
					try {
						iface_combobox.setModel(new ListComboBoxModel<Inet4Address>(NetworkInterface.networkInterfaces().filter(f -> !f.isVirtual()).flatMap(f -> f.inetAddresses())
								.filter(a -> !a.isAnyLocalAddress() && !a.isLinkLocalAddress() && !a.isLoopbackAddress()).filter(a -> a instanceof Inet4Address).map(a -> (Inet4Address) a).toList()));
					} catch (SocketException x) {
					}
				});
			}
		});
		start_button.addActionListener(e -> Util.submit(() -> {
			int port = (int) port_spinner.getValue();
			start_button.setEnabled(false);
			inspect_button.setEnabled(false);
			p_bar.setIndeterminate(true);

			Inet4Address adr = (Inet4Address) iface_combobox.getSelectedItem();
			Inet4Address[] adrs = new Inet4Address[254];
			for (int i = 0; i < adrs.length; i++) {
				try {
					adrs[i] = (Inet4Address) InetAddress.getByAddress(new byte[] { adr.getAddress()[0], adr.getAddress()[1], adr.getAddress()[2], (byte) (i + 1) });
				} catch (UnknownHostException x) {
				}
			}
			final Inet4Address[] b = Arrays.copyOf(adrs, 254);
			var f = Util.submit(() -> {
				if (CaptorTool.isJCaptorActive()) {
					return CaptorTool.filter_online_hosts(adr, b, 5000);
				} else {
					return b;
				}
			});
			try {
				adrs = f.get(6000, TimeUnit.MILLISECONDS);
			} catch (Exception x) {
			}

			Stream.of(adrs).parallel().filter(a -> {
				try {
					var socket = new Socket(a, port);
					socket.shutdownInput();
					socket.shutdownOutput();
					socket.close();
				} catch (IOException x) {
					return false;
				}
				return true;
			}).forEach(a -> Util.submit(() -> {
				String base_url = new URL("http", a.getHostAddress(), port, "").toString();
				addMinerDetailPane(base_url);
				return null;
			}));

			start_button.setEnabled(true);
			inspect_button.setEnabled(true);
			p_bar.setIndeterminate(false);
		}));
		inspect_button.addActionListener(e -> Util.submit(() -> {
			String txt = miner_url_field.getText();
			if (txt == null || txt.isBlank()) {
				return;
			}
			txt = txt.trim();
			while (txt.endsWith("/")) {
				txt = txt.substring(0, txt.length() - 1);
			}
			try {
				new URL(txt);
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), ERROR_MESSAGE);
				return;
			}
			start_button.setEnabled(false);
			inspect_button.setEnabled(false);
			p_bar.setIndeterminate(true);

			try {
				addMinerDetailPane(txt);
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), ERROR_MESSAGE);
				return;
			} finally {
				start_button.setEnabled(true);
				inspect_button.setEnabled(true);
				p_bar.setIndeterminate(false);
			}
		}));
		miner_url_field.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					inspect_button.doClick();
				}
			}
		});
		miner_url_field.setText("http://localhost:8080");
	}

	private void addMinerDetailPane(String base_path) throws Exception {
		var pane = new MinerDetailPane();
		pane.setBasePath(base_path);
		var url = new URL(base_path + StatusPane.miner_status_path);
		var auth = url.openConnection().getHeaderField("WWW-Authenticate");
		if ("Basic".equals(auth)) {
			var svc = new LoginService() {

				@Override
				public boolean authenticate(String name, char[] password, String server) throws Exception {
					var client = HttpClient.newBuilder().authenticator(new Authenticator() {
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(name, password);
						}
					}).build();
					var request = HttpRequest.newBuilder().GET().uri(url.toURI()).build();
					var response = client.send(request, BodyHandlers.discarding());
					if (response.statusCode() == 200) {
						pane.setClient(client);
						return true;
					} else {
						return false;
					}
				}
			};
			var status = JXLoginPane.showLoginDialog(getRootPane(), svc);
			if (!status.equals(Status.SUCCEEDED)) {
				return;
			}
		}
		this.pane.insertTab(rsc_bdl.getString("GENERAL.MINER"), getIcon(), pane, base_path, this.pane.getTabCount());
		Util.submit(() -> pane.actionPerformed(null));
	}

	private static final Icon getIcon() {
		return UIUtil.getStretchIcon("toolbar/" + "minecart-loaded.svg", 16, 16);
	}
}