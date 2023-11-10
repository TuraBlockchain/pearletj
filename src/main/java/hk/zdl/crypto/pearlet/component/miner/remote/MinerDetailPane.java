package hk.zdl.crypto.pearlet.component.miner.remote;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.http.HttpClient;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTabbedPane;
import javax.swing.Timer;

import hk.zdl.crypto.pearlet.component.miner.remote.conf.MinerSettingsPane;
import hk.zdl.crypto.pearlet.component.miner.remote.mining.MiningPanel;
import hk.zdl.crypto.pearlet.util.Util;

public class MinerDetailPane extends JTabbedPane implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6324183845145603011L;
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	private final Timer timer = new Timer((int) TimeUnit.SECONDS.toMillis(10), this);
	private final StatusPane status_pane = new StatusPane();
	private final MinerSettingsPane settings_pane = new MinerSettingsPane();
	private final PlotProgressPanel plot_progress_panel = new PlotProgressPanel();
	private final MiningPanel mining_panel = new MiningPanel();

	public MinerDetailPane() {
		add(rsc_bdl.getString("MINER.REMOTE.TAB.STATUS"), status_pane);
		add(rsc_bdl.getString("MINER.REMOTE.TAB.CONF"), settings_pane);
		add(rsc_bdl.getString("MINER.REMOTE.TAB.PLOT"), plot_progress_panel);
		add(rsc_bdl.getString("MINER.REMOTE.TAB.MINING"), mining_panel);

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent e) {
				timer.start();
				mining_panel.actionPerformed(null);
			}
		});
	}

	public void setBasePath(String basePath) {
		status_pane.setBasePath(basePath);
		settings_pane.setBasePath(basePath);
		plot_progress_panel.setBasePath(basePath);
		mining_panel.setBasePath(basePath);
	}

	public void setClient(HttpClient client) {
		status_pane.setClient(client);
		settings_pane.setClient(client);
		plot_progress_panel.setClient(client);
		mining_panel.setClient(client);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		status_pane.actionPerformed(e);
		mining_panel.actionPerformed(e);
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
