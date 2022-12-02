package hk.zdl.crypto.pearlet.component.miner.local;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import hk.zdl.crypto.pearlet.ui.TextAreaOutputStream;

public class MinerPanel extends JPanel implements Runnable {

	private static final long serialVersionUID = 8753464472327536267L;
	private final Process proc;

	public MinerPanel(Process proc) {
		super(new BorderLayout());
		this.proc = proc;
	}

	@Override
	public void run() {
		var txt_area = new JTextArea();
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
				}
				taos.write(line);
			}
		} catch (IOException e) {
		} finally {
			taos.close();
		}
	}

}
