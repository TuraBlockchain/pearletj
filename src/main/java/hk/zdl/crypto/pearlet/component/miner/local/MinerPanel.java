package hk.zdl.crypto.pearlet.component.miner.local;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.SwingUtilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import hk.zdl.crypto.pearlet.component.event.PlotDoneEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.ui.TextAreaOutputStream;

public class MinerPanel extends JPanel implements Runnable {

	private static final long serialVersionUID = 8753464472327536267L;
	private final File miner_bin, conf_file;
	private Collection<Path> plot_dirs = Arrays.asList();
	private boolean running = true;
	private CryptoNetwork network;
	private Process proc;

	public MinerPanel(File miner_bin, File conf_file) {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
		this.miner_bin = miner_bin;
		this.conf_file = conf_file;
	}

	public void setNetwork(CryptoNetwork network) {
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
		var w = SwingUtilities.getWindowAncestor(this);
		w.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
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
					reader = proc.inputReader(Charset.defaultCharset());
				}
				var line = reader.readLine();
				if (line == null) {
					continue;
				} else {
					line = line.trim() + "\n";
				}
				if (line.isBlank() || line.startsWith("Searching")) {
					continue;
				} else if (network.getName().startsWith("PETH")) {
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
				} else if (line.contains("secretPhrase=")) {
					continue;
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
