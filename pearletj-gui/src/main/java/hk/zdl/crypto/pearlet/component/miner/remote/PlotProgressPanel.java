package hk.zdl.crypto.pearlet.component.miner.remote;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jakewharton.byteunits.BinaryByteUnit;

import hk.zdl.crypto.pearlet.component.miner.remote.conf.MinerAccountSettingsPanel;
import hk.zdl.crypto.pearlet.component.miner.remote.conf.MinerPathSettingPanel;
import hk.zdl.crypto.pearlet.ui.ProgressBarTableCellRenderer;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class PlotProgressPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1150055243038748734L;
	private static final long byte_per_nounce = 262144;
	public static final String plot_path = "/api/v1/plot";
	private final CardLayout card = new CardLayout();
	private final JPanel top_pane = new JPanel(card);
	private final JButton add_btn = new JButton("Add a Plot");
	private final JTable table = new JTable(new DefaultTableModel(new Object[][] {}, new Object[] { "Type", "Rate", "Progress", "ETA", "Path" })) {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7710119472375311686L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	private String basePath = "";

	public PlotProgressPanel() {
		super(new BorderLayout());
		init_table();
		top_pane.add("btn", add_btn);
		var bar = new JProgressBar();
		bar.setIndeterminate(true);
		top_pane.add("bar", bar);
		add(top_pane, BorderLayout.NORTH);
		add(new JScrollPane(table), BorderLayout.CENTER);
		add_btn.addActionListener(e -> Util.submit(() -> addPlot()));
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	private void init_table() {
		table.setFillsViewportHeight(true);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setColumnSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(false);
		table.setDragEnabled(false);
		table.setRowSelectionAllowed(false);
		table.setShowGrid(true);
		table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
		table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer());
		table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer());
		((DefaultTableCellRenderer) table.getColumnModel().getColumn(0).getCellRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		((DefaultTableCellRenderer) table.getColumnModel().getColumn(1).getCellRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
		((DefaultTableCellRenderer) table.getColumnModel().getColumn(3).getCellRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
		table.getColumnModel().getColumn(2).setCellRenderer(new ProgressBarTableCellRenderer());
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
				card.show(top_pane, "bar");
				var httpclient = HttpClients.createDefault();
				var httpPost = new HttpPost(basePath + plot_path + "/add");
				var jobj = new JSONObject();
				jobj.put("id", new BigInteger(combo_box_1.getSelectedItem().toString()));
				jobj.put("nounces", slider_1.getValue() * 100);
				jobj.put("target_path", combo_box_2.getSelectedItem());
				httpPost.setEntity(new StringEntity(jobj.toString()));
				httpPost.setHeader("Content-type", "application/json");
				var response = httpclient.execute(httpPost);
				if (response.getStatusLine().getStatusCode() == 200) {
					response.close();
					UIUtil.displayMessage("Succeed", "Plot started!", null);
					Util.submit(new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							for (int i = 0; i < 5; i++) {
								refresh_current_plots();
								TimeUnit.SECONDS.sleep(1);
							}
							return null;
						}
					});
				} else {
					var text = IOUtils.readLines(response.getEntity().getContent(), "UTF-8").get(0);
					response.close();
					JOptionPane.showMessageDialog(getRootPane(), text, "Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			} finally {
				card.show(top_pane, "btn");
			}
		}
	}

	private String toTibibytesString(long val) {
		var str = new BigDecimal(BinaryByteUnit.BYTES.toGibibytes(val)).divide(new BigDecimal(1024)).toPlainString();
		if (str.contains(".")) {
			int i = str.indexOf('.');
			str = str.substring(0, Math.min(i + 4, str.length() - 1));
		}
		return str + " TiB";
	}

	public void refresh_current_plots() throws Exception {
		var in = new URL(basePath + plot_path + "/list").openStream();
		var jarr = new JSONArray(new JSONTokener(in));
		in.close();
		var model = (DefaultTableModel) table.getModel();
		model.setRowCount(jarr.length() * 2);
		for (int i = 0; i < jarr.length(); i++) {
			int row_1 = i * 2;
			int row_2 = i * 2 + 1;
			model.setValueAt("Hash", row_1, 0);
			model.setValueAt("Write", row_2, 0);
			var jobj = jarr.getJSONObject(i);
			model.setValueAt(jobj.get("hash_rate"), row_1, 1);
			model.setValueAt(jobj.get("write_rate"), row_2, 1);
			model.setValueAt(jobj.getFloat("hash_progress"), row_1, 2);
			model.setValueAt(jobj.getFloat("write_progress"), row_2, 2);
			model.setValueAt(jobj.get("hash_eta"), row_1, 3);
			model.setValueAt(jobj.get("write_eta"), row_2, 3);
			model.setValueAt(jobj.get("path"), row_1, 4);
			model.setValueAt(jobj.get("path"), row_2, 4);
		}
		SwingUtilities.invokeLater(() -> UIUtil.adjust_table_width(table, table.getColumnModel()));
	}
}
