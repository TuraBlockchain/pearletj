package hk.zdl.crypto.pearlet.component.miner.remote;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;

import org.json.JSONObject;

import hk.zdl.crypto.pearlet.component.miner.remote.conf.MinerSettingsPane;
import hk.zdl.crypto.pearlet.component.miner.remote.conf.PlotProgressPanel;

public class MinerDetailPane extends JTabbedPane implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6324183845145603011L;
	private final Timer timer = new Timer((int) TimeUnit.SECONDS.toMillis(10), this);

	private final StatusPane status_pane = new StatusPane();
	private final MinerSettingsPane settings_pane = new MinerSettingsPane();
	private final PlotProgressPanel plot_progress_panel = new PlotProgressPanel();
	private String basePath = "";

	public MinerDetailPane() {
		add("Status", status_pane);
		add("Configure", settings_pane);
		add("Plot", plot_progress_panel);
		add("Mining", new JPanel());

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent e) {
				timer.start();
			}
		});
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
		status_pane.setBasePath(basePath);
		settings_pane.setBasePath(basePath);
		plot_progress_panel.setBasePath(basePath);
	}

	public void setStatus(JSONObject status) {
		status_pane.setStatus(status);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		status_pane.actionPerformed(e);
		try {
			plot_progress_panel.refresh_current_plots();
		} catch (Exception x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
		}
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		timer.stop();
	}

}
