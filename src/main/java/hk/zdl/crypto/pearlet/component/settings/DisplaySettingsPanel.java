package hk.zdl.crypto.pearlet.component.settings;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.greenrobot.eventbus.EventBus;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.Util;

public class DisplaySettingsPanel extends JPanel {

	private static final long serialVersionUID = -5548573847414921489L;

	public DisplaySettingsPanel() {
		super(new FlowLayout());
		var panel = new JPanel(new GridLayout(0, 1));
		var cbox1 = new JCheckBox("Display PETH address as numeric ID");
		panel.add(cbox1);
		var cbox2 = new JCheckBox("Show notification for solo local miner(s)");
		panel.add(cbox2);
		var cbox3 = new JCheckBox("Show notification on plot finish");
		panel.add(cbox3);

		add(panel);

		var perf = Util.getUserSettings();
		cbox1.setSelected(perf.getBoolean(DisplaySettings.SNID, false));
		cbox1.addActionListener(e -> {
			perf.putBoolean(DisplaySettings.SNID, cbox1.isSelected());
			Util.submit(() -> {
				perf.flush();
				return null;
			});
			Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
		});

		cbox2.setSelected(perf.getBoolean(DisplaySettings.SNSM, false));
		cbox2.addActionListener(e -> {
			perf.putBoolean(DisplaySettings.SNSM, cbox2.isSelected());
			Util.submit(() -> {
				perf.flush();
				return null;
			});
		});

		cbox3.setSelected(perf.getBoolean(DisplaySettings.SNPF, false));
		cbox3.addActionListener(e -> {
			perf.putBoolean(DisplaySettings.SNPF, cbox3.isSelected());
			Util.submit(() -> {
				perf.flush();
				return null;
			});
		});
	}

}
