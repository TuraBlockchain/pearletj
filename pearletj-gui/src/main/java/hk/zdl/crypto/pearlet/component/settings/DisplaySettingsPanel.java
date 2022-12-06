package hk.zdl.crypto.pearlet.component.settings;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import hk.zdl.crypto.pearlet.util.Util;

public class DisplaySettingsPanel extends JPanel {

	private static final long serialVersionUID = -5548573847414921489L;

	public DisplaySettingsPanel() {
		super(new FlowLayout());
		var panel = new JPanel(new GridBagLayout());
		panel.add(new JLabel("Display PETH address as numberic ID"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		var cbox1 = new JCheckBox();
		panel.add(cbox1, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(panel);

		cbox1.setSelected(Boolean.parseBoolean(Util.getUserSettings().getProperty("show_numberic_id")));
		cbox1.addActionListener(e -> {
			try {
				Util.getUserSettings().setProperty("show_numberic_id", "" + cbox1.isSelected());
				Util.saveUserSettings();
			} catch (IOException x) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
			}
		});
	}

}
