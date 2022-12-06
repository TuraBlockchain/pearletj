package hk.zdl.crypto.pearlet.component.miner.local;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import hk.zdl.crypto.pearlet.component.event.PlotDoneEvent;
import hk.zdl.crypto.pearlet.ui.TextAreaOutputStream;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;

public class MinerPanel extends JPanel implements Runnable {

	private static final long serialVersionUID = 8753464472327536267L;
	private final File miner_bin, conf_file;
	private Collection<Path> plot_dirs = Arrays.asList();
	private boolean running = true;
	private CrptoNetworks network;
	private Process proc;

	public MinerPanel(File miner_bin, File conf_file) {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
		this.miner_bin = miner_bin;
		this.conf_file = conf_file;
	}

	public void setNetwork(CrptoNetworks network) {
		this.network = network;
	}

	public void setPlotDirs(Collection<Path> plot_dirs) {
		this.plot_dirs = plot_dirs;
	}

	@Override
	public void run() {
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				proc.destroyForcibly();
			}
		});
		var txt_area = new JTextArea();
		txt_area.setEditable(false);
		txt_area.setBackground(Color.black);
		txt_area.setForeground(Color.white.darker());
		txt_area.setFont(new Font(Font.MONOSPACED, Font.BOLD, getFont().getSize()));
		txt_area.setCursor(getCursor());
		var scr = new JScrollPane(txt_area);
		add(scr, BorderLayout.CENTER);
		var taos = new TextAreaOutputStream(txt_area, 200);
		try {
			BufferedReader reader = null;
			while (running) {
				if (proc == null || !proc.isAlive()) {
					proc = LocalMiner.build_process(miner_bin, conf_file);
					reader = proc.inputReader(Charset.forName("UTF-8"));
				}
				var line = reader.readLine();
				if (line == null) {
					continue;
				} else {
					line = line.trim() + "\n";
				}
				if (line.isBlank() || line.startsWith("Searching")) {
					continue;
				} else if (network == CrptoNetworks.ROTURA) {
					line = line.replace("signum-miner", "pearletj-miner");
				}
				if (line.startsWith("message:")) {
					var str = line.substring(line.indexOf('{'), line.indexOf('}') + 1);
					var jobj = new JSONObject(str);
					if (jobj.has("result")) {
						str = jobj.getString("result");
						if (str.equals("No mining licence")) {
							stop();
							proc.destroy();
						}
					}
				}
				taos.write(line);
			}
		} catch (Exception x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
		} finally {
			taos.close();
		}
	}

	public void stop() {
		running = false;
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(PlotDoneEvent e) {
		if (plot_dirs.contains(e.path)) {
			if (proc != null) {
				proc.destroyForcibly();
				proc = null;
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		EventBus.getDefault().unregister(this);
	}

}
