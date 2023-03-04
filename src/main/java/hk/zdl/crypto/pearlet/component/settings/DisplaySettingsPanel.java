package hk.zdl.crypto.pearlet.component.settings;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.greenrobot.eventbus.EventBus;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.Util;

public class DisplaySettingsPanel extends JPanel {

	private static final long serialVersionUID = -5548573847414921489L;

	public DisplaySettingsPanel() {
		super(new FlowLayout());
		var panel = new JPanel(new GridBagLayout());
		panel.add(new JLabel("Display PETH address as numeric ID"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		var cbox1 = new JCheckBox();
		panel.add(cbox1, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(panel);

		var perf = Util.getUserSettings();
		var show_numberic = perf.getBoolean("show_numberic_id", false);
		cbox1.setSelected(show_numberic);
		cbox1.addActionListener(e -> {
			perf.putBoolean("show_numberic_id", cbox1.isSelected());
			try {
				perf.flush();
			} catch (Exception x) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
			}
			Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
		});
	}

}
