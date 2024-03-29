package hk.zdl.crypto.pearlet.component.settings;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.greenrobot.eventbus.EventBus;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.util.Util;

public class DisplaySettingsPanel extends JPanel {

	private static final long serialVersionUID = -5548573847414921489L;
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();

	public DisplaySettingsPanel() {
		super(new FlowLayout());
		var panel = new JPanel(new GridLayout(0, 1));
		var cbox1 = new JCheckBox(rsc_bdl.getString("SETTINGS.DISPLAY.TURA_AS_NUM"));
		panel.add(cbox1);
		var cbox2 = new JCheckBox(rsc_bdl.getString("SETTINGS.DISPLAY.SHOW_NOTI.SOLO"));
		panel.add(cbox2);
		var cbox3 = new JCheckBox(rsc_bdl.getString("SETTINGS.DISPLAY.SHOW_NOTI.PLOT_FINISH"));
		panel.add(cbox3);
		var label_1 = new JLabel(rsc_bdl.getString("SETTINGS.DISPLAY.SHOW_NOTI.PLOT_FINISH"));
		var spinner_1 = new JSpinner(new SpinnerNumberModel(100, 100, 1000, 100));
		var panel_1 = new JPanel(new BorderLayout());
		panel_1.add(label_1, BorderLayout.WEST);
		panel_1.add(spinner_1, BorderLayout.CENTER);
		panel.add(panel_1);
		add(panel);

		var perf = Util.getUserSettings();
		cbox1.setSelected(perf.getBoolean(DisplaySettings.SNID, false));
		cbox1.addActionListener(e -> {
			perf.putBoolean(DisplaySettings.SNID, cbox1.isSelected());
			Util.submit(() -> {
				perf.flush();
				return null;
			});
			EventBus.getDefault().post(new AccountListUpdateEvent());
		});

		cbox2.setSelected(perf.getBoolean(DisplaySettings.SNSM, false));
		cbox2.addActionListener(e -> {
			perf.putBoolean(DisplaySettings.SNSM, cbox2.isSelected());
			flush();
		});

		cbox3.setSelected(perf.getBoolean(DisplaySettings.SNPF, false));
		cbox3.addActionListener(e -> {
			perf.putBoolean(DisplaySettings.SNPF, cbox3.isSelected());
			flush();
		});
		spinner_1.setValue(perf.getInt(DisplaySettings.BLOCK_COUNT, 100));
		spinner_1.addChangeListener(e -> {
			perf.putInt(DisplaySettings.BLOCK_COUNT, (Integer) spinner_1.getValue());
			flush();
		});
	}

	private static final void flush() {
		Util.submit(() -> {
			Util.getUserSettings().flush();
			return null;
		});
	}
}
