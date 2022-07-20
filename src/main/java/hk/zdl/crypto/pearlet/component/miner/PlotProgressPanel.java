package hk.zdl.crypto.pearlet.component.miner;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.Random;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import com.jakewharton.byteunits.BinaryByteUnit;

public class PlotProgressPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1150055243038748734L;
	private static final long byte_per_nounce = 262144;
	public static final String plot_path = "/api/v1/plot";
	private final JButton add_btn = new JButton("Add a Plot");
	private final JTable table = new JTable(6, 6);

	private String basePath = "";

	public PlotProgressPanel() {
		super(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Plot", TitledBorder.CENTER, TitledBorder.TOP, MinerGridTitleFont.getFont()));
		add(add_btn, BorderLayout.NORTH);
		add(table, BorderLayout.CENTER);
		add_btn.addActionListener(e -> addPlot());
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	private final void addPlot() {
		var inset_5 = new Insets(5, 5, 5, 5);
		var panel = new JPanel(new GridBagLayout());
		var label_1 = new JLabel("id:");
		panel.add(label_1, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, inset_5, 0, 0));
		var combo_box_1 = new JComboBox<String>();
		panel.add(combo_box_1, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, inset_5, 0, 0));
		var label_2 = new JLabel("Path:");
		panel.add(label_2, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, inset_5, 0, 0));
		var combo_box_2 = new JComboBox<String>();
		panel.add(combo_box_2, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, inset_5, 0, 0));
		var label_3 = new JLabel("Start Nounce:");
		panel.add(label_3, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, inset_5, 0, 0));
		var spinner_1 = new JSpinner(new SpinnerNumberModel(Math.abs(new Random().nextInt()), 1, Integer.MAX_VALUE, 1));
		panel.add(spinner_1, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, inset_5, 0, 0));
		var label_4 = new JLabel("Nounces:");
		panel.add(label_4, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, inset_5, 0, 0));
		var slider_1 = new JSlider(1, 100000, 1000);
		panel.add(slider_1, new GridBagConstraints(1, 3, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, inset_5, 0, 0));
		var label_5 = new JLabel("File Size:");
		panel.add(label_5, new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, inset_5, 0, 0));
		var label_6 = new JLabel();
		panel.add(label_6, new GridBagConstraints(1, 4, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, inset_5, 0, 0));
		slider_1.addChangeListener(e -> {
			long bytes = slider_1.getValue()*100 * byte_per_nounce;
			var strs = new String[] { bytes + " B", BinaryByteUnit.BYTES.toKibibytes(bytes) + " KiB", BinaryByteUnit.BYTES.toMebibytes(bytes) + " MiB",
					BinaryByteUnit.BYTES.toGibibytes(bytes) + " GiB", toTibibytesString(bytes), };
			var str = Stream.of(strs).filter(s -> Float.parseFloat(s.split(" ")[0]) < 1024).findFirst().get();
			label_6.setText(str);
		});
		slider_1.getChangeListeners()[0].stateChanged(null);

		int i = JOptionPane.showConfirmDialog(getRootPane(), panel, "Add a Plot", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	}

	private String toTibibytesString(long val) {
		var str = new BigDecimal(BinaryByteUnit.BYTES.toGibibytes(val)).divide(new BigDecimal(1024)).toPlainString();
		if (str.contains(".")) {
			int i = str.indexOf('.');
			str = str.substring(0, i + 4);
		}
		return str + " TiB";
	}
}
