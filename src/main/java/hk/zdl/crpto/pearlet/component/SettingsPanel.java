package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import hk.zdl.crpto.pearlet.MyToolbar;
import hk.zdl.crpto.pearlet.component.event.SettingsPanelEvent;
import hk.zdl.crpto.pearlet.misc.AccountTableModel;
import hk.zdl.crpto.pearlet.misc.IndepandentWindows;
import hk.zdl.crpto.pearlet.util.CryptoUtil;

@SuppressWarnings("serial")
public class SettingsPanel extends JTabbedPane {

	private static List<String> supported_networks;
	static {
		try {
			supported_networks = IOUtils.readLines(SettingsPanel.class.getClassLoader().getResourceAsStream("networks.txt"), "UTF-8");
		} catch (IOException e) {
			supported_networks = Arrays.asList();
		}
	}

	public SettingsPanel() {
		addTab(SettingsPanelEvent.NET, initNetworkPanel());
		addTab(SettingsPanelEvent.ACC, initAccountPanel());
		EventBus.getDefault().register(this);
	}

	private static final Component initNetworkPanel() {
		var panel = new JPanel(new GridLayout(0, 1));
		supported_networks.stream().map(SettingsPanel::init_network_UI_components).forEach(panel::add);
		var panel_1 = new JPanel(new FlowLayout());
		panel_1.add(panel);
		return panel_1;
	}

	@SuppressWarnings("unchecked")
	private static final Component init_network_UI_components(String network_name) {
		var panel = new JPanel(new GridBagLayout());
		var label = new JLabel(network_name);
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
			nws = IOUtils.readLines(SettingsPanel.class.getClassLoader().getResourceAsStream("network/" + network_name + ".txt"), "UTF-8");
		} catch (IOException e) {
		}
		combo_box.setModel(new ListComboBoxModel<String>(nws));
		if (network_name.equals("web3j")) {
			var opt_btn = new JButton("ID / Secret");
			panel.add(opt_btn, new GridBagConstraints(1, 0, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
			opt_btn.addActionListener(e -> createWeb3jAuthDialog(panel));
		}
		return panel;
	}

	private static final void createWeb3jAuthDialog(Component c) {
		var w = SwingUtilities.getWindowAncestor(c);
		var dialog = new JDialog(w, "Enter Project ID & Secret", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		IndepandentWindows.add(dialog);
		var panel_1 = new JPanel(new GridBagLayout());
		try {
			panel_1.add(new JLabel(new MyStretchIcon(ImageIO.read(MyToolbar.class.getClassLoader().getResource("icon/" + "key_1.svg")), 64, 64)),
					new GridBagConstraints(0, 0, 1, 4, 0, 0, 17, 0, new Insets(0, 5, 5, 5), 0, 0));
		} catch (IOException e) {
		}
		panel_1.add(new JLabel("Project ID:"), new GridBagConstraints(1, 0, 1, 1, 0, 0, 17, 0, new Insets(0, 5, 5, 5), 0, 0));
		var id_field = new JTextField(30);
		panel_1.add(id_field, new GridBagConstraints(1, 1, 1, 1, 0, 0, 17, 0, new Insets(0, 5, 5, 5), 0, 0));
		panel_1.add(new JLabel("Project Secret:"), new GridBagConstraints(1, 2, 1, 1, 0, 0, 17, 0, new Insets(0, 0, 5, 0), 0, 0));
		var scret_field = new JPasswordField(30);
		panel_1.add(scret_field, new GridBagConstraints(1, 3, 1, 1, 0, 0, 17, 0, new Insets(0, 5, 5, 5), 0, 0));
		var btn_1 = new JButton("OK");
		btn_1.addActionListener(e -> {
			dialog.dispose();
			// TODO: update data
		});
		panel_1.add(btn_1, new GridBagConstraints(0, 4, 2, 1, 0, 0, 10, 0, new Insets(5, 5, 10, 5), 0, 0));
		dialog.add(panel_1);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(w);
		dialog.setVisible(true);
	}

	private static final Component initAccountPanel() {
		var panel = new JPanel(new BorderLayout());
		var acc_mable_model = new AccountTableModel();
		var table_1 = new JTable(acc_mable_model);
		table_1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table_1.setShowGrid(true);
		var scr_1 = new JScrollPane(table_1);
		panel.add(scr_1, BorderLayout.CENTER);

		var btn_panel = new JPanel(new GridBagLayout());
		var create_account_btn = new JButton("Create");
		btn_panel.add(create_account_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
		var import_account_btn = new JButton("Import");
		btn_panel.add(import_account_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
		var watch_account_btn = new JButton("Watch");
		btn_panel.add(watch_account_btn, new GridBagConstraints(0, 2, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
		var del_btn = new JButton("Delete");
		btn_panel.add(del_btn, new GridBagConstraints(0, 3, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));

		create_account_btn.addActionListener(e -> {
			// TODO:
		});

		import_account_btn.addActionListener(e -> create_import_account_dialog(panel));

		watch_account_btn.addActionListener(e -> {
			// TODO:
		});

		del_btn.addActionListener(e -> {
			int i = JOptionPane.showConfirmDialog(panel.getRootPane(), "Are you sure to delete this?", "", JOptionPane.YES_NO_OPTION);
			if (i == 0) {
				// TODO: perform delete entry
			}
		});

		var panel_1 = new JPanel(new FlowLayout(1, 0, 0));
		panel_1.add(btn_panel);
		panel.add(panel_1, BorderLayout.EAST);
		return panel;
	}

	@SuppressWarnings("unchecked")
	private static final void create_import_account_dialog(Component c) {
		var w = SwingUtilities.getWindowAncestor(c);
		var dialog = new JDialog(w, "Import Existing Account", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		IndepandentWindows.add(dialog);
		var panel = new JPanel(new GridBagLayout());
		try {
			panel.add(new JLabel(new MyStretchIcon(ImageIO.read(MyToolbar.class.getClassLoader().getResource("icon/" + "wallet_2.svg")), 64, 64)),
					new GridBagConstraints(0, 0, 1, 4, 0, 0, 17, 0, new Insets(5, 5, 5, 5), 0, 0));
		} catch (IOException e) {
		}
		var label_1 = new JLabel("Network:");
		var network_combobox = new JComboBox<>();
		network_combobox.setModel(new ListComboBoxModel<String>(supported_networks));
		var label_2 = new JLabel("Text type:");
		var combobox_1 = new JComboBox<>(new String[] { "Phrase" });
		panel.add(label_1, new GridBagConstraints(1, 0, 1, 1, 0, 0, 17, 0, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(network_combobox, new GridBagConstraints(2, 0, 1, 1, 0, 0, 17, 0, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(label_2, new GridBagConstraints(3, 0, 1, 1, 0, 0, 17, 0, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(combobox_1, new GridBagConstraints(4, 0, 1, 1, 0, 0, 17, 0, new Insets(5, 5, 5, 5), 0, 0));
		var text_area = new JTextArea(5, 30);
		var scr_pane = new JScrollPane(text_area);
		panel.add(scr_pane, new GridBagConstraints(1, 1, 4, 3, 0, 0, 17, 0, new Insets(5, 5, 0, 5), 0, 0));
		var btn_1 = new JButton("OK");
		btn_1.addActionListener(e -> {
			String nw = network_combobox.getSelectedItem().toString();
			String type = combobox_1.getSelectedItem().toString();
			String text = text_area.getText().trim();
			
			dialog.dispose();
			// TODO: update data
		});
		var panel_1 = new JPanel(new BorderLayout());
		panel_1.add(panel, BorderLayout.CENTER);
		var panel_2 = new JPanel(new FlowLayout());
		panel_2.add(btn_1);
		panel_1.add(panel_2, BorderLayout.SOUTH);

		dialog.add(panel_1);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(w);
		dialog.setVisible(true);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(SettingsPanelEvent e) {
		setSelectedIndex(indexOfTab(e.getString()));
	}
}
