package hk.zdl.crpto.pearlet.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.apache.commons.io.IOUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

@SuppressWarnings("serial")
public class SettingsPanel extends JTabbedPane {

	public SettingsPanel() {
		addTab("Networks", initNetworkPanel());
		addTab("Accounts", initAccountPanel());
	}

	private static final Component initNetworkPanel() {
		var panel = new JPanel(new GridLayout(0, 1));
		List<String> nws = Arrays.asList();
		try {
			nws = IOUtils.readLines(SettingsPanel.class.getClassLoader().getResourceAsStream("networks.txt"),"UTF-8");
		} catch (IOException e) {
		}
		nws.stream().map(SettingsPanel::init_network_UI_components).forEach(panel::add);
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
		panel.add(combo_box, new GridBagConstraints(0, 1, 1, 1, 0, 0, 10, 0, new Insets(5, 0, 5, 0), 0, 0));
		var btn = new JButton("Select Node");
		panel.add(btn, new GridBagConstraints(1, 1, 1, 1, 0, 0, 10, 0, new Insets(5, 5, 5, 5), 0, 0));
		List<String> nws = Arrays.asList();
		try {
			nws = IOUtils.readLines(SettingsPanel.class.getClassLoader().getResourceAsStream("network/"+network_name+".txt"),"UTF-8");
		} catch (IOException e) {
		}
		combo_box.setModel(new ListComboBoxModel<String>(nws));
		return panel;
	}

	private static final Component initAccountPanel() {
		var panel = new JPanel();
		return panel;
	}
}
