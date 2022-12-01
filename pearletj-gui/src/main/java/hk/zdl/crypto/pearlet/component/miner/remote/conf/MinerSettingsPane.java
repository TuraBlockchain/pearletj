package hk.zdl.crypto.pearlet.component.miner.remote.conf;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.Timer;

public class MinerSettingsPane extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3040618948690123951L;
	private final MinerAccountSettingsPanel account_settings_panel = new MinerAccountSettingsPanel();
	private final MinerMiscSettingsPane misc_settings_panel = new MinerMiscSettingsPane();
	private final MinerPathSettingPanel miner_path_settings_panel = new MinerPathSettingPanel();
	private final PlotProgressPanel plot_progress_panel = new PlotProgressPanel();
	private final Timer timer = new Timer((int) TimeUnit.SECONDS.toMillis(10), this);

	public MinerSettingsPane() {
		super(new GridLayout(2, 2));
		init_panels();
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent e) {
				timer.start();
				actionPerformed(null);
			}
		});
	}

	private void init_panels() {
		add(account_settings_panel);
		add(misc_settings_panel);
		add(miner_path_settings_panel);
		add(plot_progress_panel);
	}

	public void setBasePath(String basePath) {
		account_settings_panel.setBasePath(basePath);
		misc_settings_panel.setBasePath(basePath);
		miner_path_settings_panel.setBasePath(basePath);
		plot_progress_panel.setBasePath(basePath);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			account_settings_panel.refresh_list();
		} catch (Exception x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
		}
		try {
			misc_settings_panel.update_server_address();
		} catch (Exception x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
		}
		try {
			miner_path_settings_panel.refresh_list();
		} catch (Exception x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
		}
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
