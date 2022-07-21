package hk.zdl.crypto.pearlet.component.miner.conf;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.Callable;
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
import javax.swing.table.DefaultTableModel;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jakewharton.byteunits.BinaryByteUnit;

import hk.zdl.crypto.pearlet.component.miner.MinerGridTitleFont;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class PlotProgressPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1150055243038748734L;
	private static final long byte_per_nounce = 262144;
	public static final String plot_path = "/api/v1/plot";
	private final JButton add_btn = new JButton("Add a Plot");
	private final JTable table = new JTable(new PlotTableModel());

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
			long bytes = slider_1.getValue() * 100 * byte_per_nounce;
			var strs = new String[] { bytes + " B", BinaryByteUnit.BYTES.toKibibytes(bytes) + " KiB", BinaryByteUnit.BYTES.toMebibytes(bytes) + " MiB",
					BinaryByteUnit.BYTES.toGibibytes(bytes) + " GiB", toTibibytesString(bytes), };
			var str = Stream.of(strs).filter(s -> Float.parseFloat(s.split(" ")[0]) < 1024).findFirst().get();
			label_6.setText(str);
		});
		slider_1.getChangeListeners()[0].stateChanged(null);
		Util.submit(new Callable<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void call() throws Exception {
				combo_box_1.setModel(new ListComboBoxModel<String>(
						new JSONArray(new JSONTokener(new URL(basePath + MinerAccountSettingsPanel.miner_account_path).openStream())).toList().stream().map(String::valueOf).toList()));
				combo_box_2.setModel(new ListComboBoxModel<String>(
						new JSONArray(new JSONTokener(new URL(basePath + MinerPathSettingPanel.miner_file_path + "/list").openStream())).toList().stream().map(String::valueOf).toList()));

				return null;
			}
		});

		int i = JOptionPane.showConfirmDialog(getRootPane(), panel, "Add a Plot", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == JOptionPane.OK_OPTION && combo_box_1.getSelectedIndex() > -1 && combo_box_2.getSelectedIndex() > -1) {
			try {
				var httpclient = HttpClients.createDefault();
				var httpPost = new HttpPost(basePath + plot_path + "/add");
				var jobj = new JSONObject();
				jobj.put("id", new BigInteger(combo_box_1.getSelectedItem().toString()));
				jobj.put("start_nounce", spinner_1.getValue());
				jobj.put("nounces", slider_1.getValue() * 100);
				httpPost.setEntity(new StringEntity(jobj.toString()));
				var response = httpclient.execute(httpPost);
				response.close();
				if (response.getStatusLine().getStatusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Add plot succeed!", null);
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	private String toTibibytesString(long val) {
		var str = new BigDecimal(BinaryByteUnit.BYTES.toGibibytes(val)).divide(new BigDecimal(1024)).toPlainString();
		if (str.contains(".")) {
			int i = str.indexOf('.');
			str = str.substring(0, i + 4);
		}
		return str + " TiB";
	}

	public void refresh_current_plots() throws Exception {
		var jarr = new JSONArray(new JSONTokener(new URL(basePath + plot_path + "/list").openStream()));
	}

	private static final class PlotTableModel extends DefaultTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5679611176730255612L;

		public PlotTableModel() {
			super(new Object[] { "Type", "Rate", "Progress", "ETA", "Path" }, 0);
		}

	}
}
