package hk.zdl.crypto.pearlet.component.miner.local;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import hk.zdl.crypto.pearlet.ui.TextAreaOutputStream;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;

public class MinerPanel extends JPanel implements Runnable {

	private static final long serialVersionUID = 8753464472327536267L;
	private static final boolean show_peth_only = Util.getProp().getBoolean("show_peth_only");
	private final Process proc;
	private CrptoNetworks network;

	public MinerPanel(Process proc) {
		super(new BorderLayout());
		this.proc = proc;
	}

	public void setNetwork(CrptoNetworks network) {
		this.network = network;
	}

	@Override
	public void run() {
		var txt_area = new JTextArea();
		txt_area.setBackground(Color.black);
		txt_area.setForeground(Color.white.darker());
		txt_area.setFont(new Font(Font.MONOSPACED, Font.BOLD, getFont().getSize()));
		var scr = new JScrollPane(txt_area);
		add(scr, BorderLayout.CENTER);
		var taos = new TextAreaOutputStream(txt_area);
		var in = proc.inputReader(Charset.forName("UTF-8"));
		try {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> proc.destroy()));
			while (proc.isAlive()) {
				var line = in.readLine().trim() + "\n";
				if (line.isBlank() || line.startsWith("Searching")) {
					continue;
				} else if (show_peth_only && network == CrptoNetworks.ROTURA) {
					line = line.replace("signum-miner", "pearletj-miner");
				}
				taos.write(line);
			}
		} catch (IOException e) {
		} finally {
			taos.close();
		}
	}

	public void destroy() {
		proc.destroy();
	}

	public Process destroyForcibly() {
		return proc.destroyForcibly();
	}

}
