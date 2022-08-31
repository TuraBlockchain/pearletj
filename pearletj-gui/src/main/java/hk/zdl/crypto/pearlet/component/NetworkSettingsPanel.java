package hk.zdl.crypto.pearlet.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class NetworkSettingsPanel extends JPanel {

	public NetworkSettingsPanel() {
		super(new FlowLayout(FlowLayout.CENTER));
		var panel = new JPanel(new GridLayout(0, 1));
		Stream.of(CrptoNetworks.values()).map(NetworkSettingsPanel::init_network_UI_components).forEach(panel::add);
		add(panel);
	}

	@SuppressWarnings("unchecked")
	private static final Component init_network_UI_components(CrptoNetworks network_name) {
		var panel = new JPanel(new GridBagLayout());
		var label = new JLabel(network_name.name());
		if(network_name.equals(CrptoNetworks.ROTURA)) {
			label.setText("PETH");
		}
		label.setHorizontalTextPosition(SwingConstants.LEFT);
		panel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, 17, 0, new Insets(5, 0, 0, 0), 0, 0));
		var combo_box = new JComboBox<String>();
		combo_box.setEditable(true);
		combo_box.setPreferredSize(new Dimension(300, 20));
		panel.add(combo_box, new GridBagConstraints(0, 1, 3, 1, 0, 0, 10, 0, new Insets(5, 0, 5, 0), 0, 0));
		var btn = new JButton("Select Node");
		panel.add(btn, new GridBagConstraints(3, 1, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
		List<String> nws = Arrays.asList();
		try {
			nws = IOUtils.readLines(Util.getResourceAsStream("network/" + network_name.name().toLowerCase() + ".txt"), "UTF-8");
		} catch (IOException e) {
		}
		combo_box.setModel(new ListComboBoxModel<String>(nws));
		if (network_name.equals(CrptoNetworks.WEB3J)) {
			label.setText("Ethereum");
			var opt_btn = new JButton("ID / Secret");
			panel.add(opt_btn, new GridBagConstraints(1, 0, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
			opt_btn.addActionListener(e -> createWeb3jAuthDialog(panel));
		}
		Util.submit(() -> MyDb.get_server_url(network_name).ifPresent(combo_box::setSelectedItem));
		btn.addActionListener(e -> Util.submit(() -> {
			try {
				URL url = new URL(combo_box.getSelectedItem().toString().trim());
				var p = url.getProtocol().toLowerCase();
				if (!p.equals("http") && !p.equals("https")) {
					JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(panel), "Unsupported Protocol" + ": " + p, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				boolean b = MyDb.update_server_url(network_name, url.toString());
				if (b) {
					JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(panel), "Node updated.", null, JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(panel), "Something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (MalformedURLException x) {
				JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(panel), "Invalid URL", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}));
		return panel;
	}

	private static final void createWeb3jAuthDialog(Component c) {
		var w = SwingUtilities.getWindowAncestor(c);
		var panel_1 = new JPanel(new GridBagLayout());
		panel_1.add(new JLabel("Project ID:"), new GridBagConstraints(0, 0, 1, 1, 0, 0, 17, 0, new Insets(0, 5, 5, 5), 0, 0));
		var id_field = new JTextField("<Your ID here>", 30);
		panel_1.add(id_field, new GridBagConstraints(0, 1, 1, 1, 0, 0, 17, 0, new Insets(0, 5, 5, 5), 0, 0));
		panel_1.add(new JLabel("Project Secret:"), new GridBagConstraints(0, 2, 1, 1, 0, 0, 17, 0, new Insets(0, 5, 5, 0), 0, 0));
		var scret_field = new JPasswordField("unchanged", 30);
		panel_1.add(scret_field, new GridBagConstraints(0, 3, 1, 1, 0, 0, 17, 0, new Insets(0, 5, 5, 5), 0, 0));
		MyDb.get_webj_auth().ifPresent(r -> id_field.setText(r.getStr("MYAUTH")));

		int i = JOptionPane.showConfirmDialog(w, panel_1, "Enter Project ID & Secret", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, UIUtil.getStretchIcon("icon/" + "key_1.svg", 64, 64));
		if (i != JOptionPane.OK_OPTION) {
			return;
		}
		boolean b = MyDb.update_webj_auth(id_field.getText(), new String(scret_field.getPassword()));
		if (b) {
			CryptoUtil.clear_web3j();
		}
	}

}
