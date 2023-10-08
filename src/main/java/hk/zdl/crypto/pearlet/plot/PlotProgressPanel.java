package hk.zdl.crypto.pearlet.plot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

@SuppressWarnings("serial")
public abstract class PlotProgressPanel extends JPanel implements PlotProgressListener {
	private final JLabel hash_label_1 = new JLabel();
	private final JLabel hash_label_4 = new JLabel();
	private final JProgressBar hash_progress_bar = new JProgressBar();

	private final JLabel writ_label_1 = new JLabel();
	private final JLabel writ_label_4 = new JLabel();
	private final JProgressBar writ_progress_bar = new JProgressBar();
	private boolean done = false;

	public PlotProgressPanel() throws Exception {
		super(new GridBagLayout());
		var icon_label = new JLabel(new ImageIcon(ImageIO.read(Main.class.getClassLoader().getResource("plot_icon.png"))));
		add(icon_label, new GridBagConstraints(0, 0, 5, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 1, 1));
		var hash_label_0 = new JLabel("Hash Rate:");
		add(hash_label_0, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 1, 1));
		add(hash_label_1, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 1, 1));
		var hash_label_2 = new JLabel();
		add(hash_label_2, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 1, 1));
		var hash_label_3 = new JLabel("Time Remaining:");
		add(hash_label_3, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 1, 1));
		add(hash_label_4, new GridBagConstraints(4, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 1, 1));
		hash_progress_bar.setStringPainted(true);
		add(hash_progress_bar, new GridBagConstraints(0, 2, 5, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 1, 1));

		var writ_label_0 = new JLabel("Write Rate:");
		add(writ_label_0, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 1, 1));
		add(writ_label_1, new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 1, 1));
		var writ_label_2 = new JLabel();
		add(writ_label_2, new GridBagConstraints(2, 3, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 1, 1));
		var writ_label_3 = new JLabel("Time Remaining:");
		add(writ_label_3, new GridBagConstraints(3, 3, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 1, 1));
		add(writ_label_4, new GridBagConstraints(4, 3, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 1, 1));
		writ_progress_bar.setStringPainted(true);
		add(writ_progress_bar, new GridBagConstraints(0, 4, 5, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 1, 1));

	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
		Stream.of(hash_label_1,hash_label_4,writ_label_1,writ_label_4).forEach(o->o.setText("Unknown"));
		Stream.of(hash_progress_bar,writ_progress_bar).forEach(o->o.setValue(0));
	}

	@Override
	public void onProgress(Type type, float progress, String rate, String ETA) {
		if(done) {
			return;
		}
		if (ETA.isBlank()) {
			ETA = "0s";
		}
		switch (type) {
		case HASH:
			hash_progress_bar.setValue((int) progress);
			hash_label_1.setText(rate);
			hash_label_4.setText(ETA);
			break;
		case WRIT:
			writ_progress_bar.setValue((int) progress);
			writ_label_1.setText(rate);
			writ_label_4.setText(ETA);
			if (progress >= 100 && !done) {
				done = true;
				onDone();
			}
			break;
		default:
			break;

		}
	}
	
	public abstract void onDone();

}
