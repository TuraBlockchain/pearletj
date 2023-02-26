package hk.zdl.crypto.pearlet.component.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import hk.zdl.crypto.pearlet.component.settings.wizard.ChooseNetworkType;
import hk.zdl.crypto.pearlet.component.settings.wizard.ConfirmNetwork;
import hk.zdl.crypto.pearlet.component.settings.wizard.EnterNetworkDetail;
import hk.zdl.crypto.pearlet.component.settings.wizard.JDialogWizard;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.misc.VerticalFlowLayout;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class NetworkSettingsPanel extends JPanel {

	private static final Insets insets_5 = new Insets(2, 3, 3, 3);
	private static final JPanel center_panel = new JPanel(new GridLayout(0, 1));
	private static final int ping_timeout = 5000;

	public NetworkSettingsPanel() {
		super(new BorderLayout());

		var btn_panel = new JPanel(new VerticalFlowLayout());
		var add_btn = new JButton(UIUtil.getStretchIcon("icon/heavy-plus-sign-svgrepo-com.svg", 32, 32));
		btn_panel.add(add_btn);

		add_btn.addActionListener(e -> {
			var startPage = new ChooseNetworkType();
			var detailPage = new EnterNetworkDetail();
			startPage.setNextPage(detailPage);
			var confPage = new ConfirmNetwork();
			detailPage.setNextPage(confPage);
			var finish = JDialogWizard.showWizard("Add a Network", startPage, this);
			if (finish) {
				var new_network = new CryptoNetwork();
				new_network.setName(confPage.getNetworkName());
				new_network.setUrl(confPage.getNetworkAddress());
				switch (confPage.getType()) {
				case "Ethereum":
					new_network.setType(CryptoNetwork.Type.WEB3J);
					break;
				case "Burst Variant":
					new_network.setType(CryptoNetwork.Type.BURST);
					break;
				}
				if (MyDb.insert_network(new_network)) {
					refresh_network_list();
				}
			}
		});

		var panel_1 = new JPanel(new FlowLayout(1, 0, 0));
		panel_1.add(btn_panel);
		add(panel_1, BorderLayout.EAST);

		var p = new JPanel();
		p.add(center_panel);
		add(new JScrollPane(p, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		refresh_network_list();
	}

	private final void refresh_network_list() {
		SwingUtilities.invokeLater(() -> {
			center_panel.removeAll();
			MyDb.get_networks().stream().map(this::init_network_UI_components).forEach(center_panel::add);
			SwingUtilities.updateComponentTreeUI(this);
		});
	}

	private final Component init_network_UI_components(CryptoNetwork o) {
		var panel = new JPanel(new BorderLayout());
		panel.setPreferredSize(new Dimension(700, 130));
		var icon = new JLabel(UIUtil.getStretchIcon("icon/" + UIUtil.get_icon_file_name(o), 64, 64));
		panel.add(icon, BorderLayout.WEST);
		var my_panel = new JPanel(new GridBagLayout());
		panel.add(my_panel, BorderLayout.CENTER);
		my_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), o.getName(), TitledBorder.LEFT, TitledBorder.TOP,
				new Font("Arial Black", Font.PLAIN, (int) (getFont().getSize() * 1.5))));
		var label_0 = new JLabel("Type:");
		var label_1 = new JLabel("URL:");
		var label_2 = new JLabel("Ping:");

		var label_3 = new JLabel(o.getType().name());
		var label_4 = new JLabel(o.getUrl());
		var prog = new JProgressBar();
		prog.setString("");
		prog.setStringPainted(true);

		var btn_0 = new JButton("Delete");
		var btn_1 = new JButton("Modify");
		var btn_2 = new JButton("Ping");
		var btn_3 = new JButton("Credential...");
		if (o.getType() == CryptoNetwork.Type.WEB3J) {
			my_panel.add(btn_3, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets_5, 0, 0));
			btn_3.addActionListener(e -> createWeb3jAuthDialog(this));
		}
		my_panel.add(label_0, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		my_panel.add(label_1, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		my_panel.add(label_2, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));

		my_panel.add(label_3, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		my_panel.add(label_4, new GridBagConstraints(1, 1, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		my_panel.add(prog, new GridBagConstraints(1, 2, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

		my_panel.add(btn_0, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		my_panel.add(btn_1, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		my_panel.add(btn_2, new GridBagConstraints(3, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

		btn_0.addActionListener(e -> {
			if (JOptionPane.showConfirmDialog(getRootPane(), "Are you sure to delete this?", null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
				MyDb.delete_network(o.getId());
				refresh_network_list();
			}
		});
		btn_2.addActionListener(e -> {
			btn_2.setEnabled(false);
			prog.setString("");
			prog.setIndeterminate(true);
			Util.submit(() -> {
				var f = Util.submit(() -> {
					var t = System.currentTimeMillis();
					new URL(o.getUrl()).openStream().close();
					t = System.currentTimeMillis() - t;
					prog.setIndeterminate(false);
					prog.setString("" + t + "ms");
					btn_2.setEnabled(true);
					return null;
				});
				try {
					f.get(ping_timeout, TimeUnit.MILLISECONDS);
				} catch (TimeoutException x) {
					prog.setString(">" + ping_timeout + "ms");
				} catch (Throwable x) {
					while (x.getCause() != null) {
						x = x.getCause();
					}
					prog.setString(x.getMessage());
				} finally {
					prog.setIndeterminate(false);
					btn_2.setEnabled(true);
				}
			});
		});
		btn_1.addActionListener(e -> {
			var str = JOptionPane.showInputDialog(getRootPane(), "Please input node server URL:", null, JOptionPane.INFORMATION_MESSAGE, null, null, o.getUrl());
			if (str == null) {
				return;
			}
			try {
				new URL(str.toString());
			} catch (Throwable x) {
				return;
			}
			o.setUrl(str.toString());
			MyDb.update_network(o);
			refresh_network_list();
		});
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
