package hk.zdl.crypto.pearlet.component.miner.remote;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
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
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private final JButton add_btn = new JButton("Add");
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
		add(new JScrollPane(table), BorderLayout.CENTER);
		var btn_panel = new JPanel(new GridBagLayout());
		btn_panel.add(add_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var panel_1 = new JPanel(new FlowLayout(1, 0, 0));
		panel_1.add(btn_panel);
		add(panel_1, BorderLayout.EAST);
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
		var panel = new JPanel(new GridBagLayout());
		var label_1 = new JLabel("id:");
		panel.add(label_1, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		var combo_box_1 = new JComboBox<String>();
		panel.add(combo_box_1, new GridBagConstraints(1, 0, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var label_2 = new JLabel("Path:");
		panel.add(label_2, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		var combo_box_2 = new JComboBox<String>();
		panel.add(combo_box_2, new GridBagConstraints(1, 1, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var label_4 = new JLabel("File Size:");
		panel.add(label_4, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		var fz_spinner = new JSpinner(new SpinnerNumberModel(100, 1, 1024, 1));
		var fz_op = new JComboBox<>(new String[] { "MB", "GB" });
		panel.add(fz_spinner, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		panel.add(fz_op, new GridBagConstraints(2, 2, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets_5, 0, 0));
		var chech_box_1 = new JCheckBox("Restart miner on plot finish");
		panel.add(chech_box_1, new GridBagConstraints(0, 3, 3, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		Util.submit(new Callable<Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public Void call() throws Exception {
				combo_box_1.setModel(new ListComboBoxModel<String>(
						new JSONArray(new JSONTokener(new URL(basePath + MinerAccountSettingsPanel.miner_account_path).openStream())).toList().stream().map(String::valueOf).toList()));
				combo_box_1.getActionListeners()[0].actionPerformed(null);
				return null;
			}
		});
		combo_box_1.addActionListener(e -> {
			Util.submit(new Callable<Void>() {

				@SuppressWarnings("unchecked")
				@Override
				public Void call() throws Exception {
					combo_box_2.setModel(new ListComboBoxModel<String>(
							new JSONArray(new JSONTokener(new URL(basePath + MinerPathSettingPanel.miner_file_path + "/list?id=" + combo_box_1.getSelectedItem()).openStream())).toList().stream()
									.map(String::valueOf).toList()));

					return null;
				}
			});
		});

		int i = JOptionPane.showConfirmDialog(getRootPane(), panel, "Add a Plot", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == JOptionPane.OK_OPTION && combo_box_1.getSelectedIndex() > -1 && combo_box_2.getSelectedIndex() > -1) {
			long l = (Integer) fz_spinner.getValue();
			if (fz_op.getSelectedItem().equals("MB")) {
				l = BinaryByteUnit.MEBIBYTES.toBytes(l);
			} else if (fz_op.getSelectedItem().equals("GB")) {
				l = BinaryByteUnit.GIBIBYTES.toBytes(l);
			}
			l = l / byte_per_nounce;

			if (l < 10) {
				JOptionPane.showMessageDialog(getRootPane(), "File size too small!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			try {
				var httpclient = HttpClients.createDefault();
				var httpPost = new HttpPost(basePath + plot_path + "/add");
				var jobj = new JSONObject();
				jobj.put("id", new BigInteger(combo_box_1.getSelectedItem().toString()));
				jobj.put("nounces", l);
				jobj.put("target_path", combo_box_2.getSelectedItem());
				jobj.put("restart", chech_box_1.isSelected());
				httpPost.setEntity(new StringEntity(jobj.toString()));
				httpPost.setHeader("Content-type", "application/json");
				var response = httpclient.execute(httpPost);
				if (response.getStatusLine().getStatusCode() == 200) {
					response.close();
					UIUtil.displayMessage("Succeed", "Plot queued!", null);
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
					var text = IOUtils.readLines(response.getEntity().getContent(), Charset.defaultCharset()).get(0);
					response.close();
					JOptionPane.showMessageDialog(getRootPane(), text, "Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		}
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
			var jobj = jarr.optJSONObject(i);
			if(jobj==null) {
				jobj = new JSONObject();
			}
			model.setValueAt(jobj.opt("hash_rate"), row_1, 1);
			model.setValueAt(jobj.opt("write_rate"), row_2, 1);
			model.setValueAt(jobj.optFloat("hash_progress"), row_1, 2);
			model.setValueAt(jobj.optFloat("write_progress"), row_2, 2);
			model.setValueAt(jobj.opt("hash_eta"), row_1, 3);
			model.setValueAt(jobj.opt("write_eta"), row_2, 3);
			model.setValueAt(jobj.opt("path"), row_1, 4);
			model.setValueAt(jobj.opt("path"), row_2, 4);
		}
		SwingUtilities.invokeLater(() -> UIUtil.adjust_table_width(table, table.getColumnModel()));
	}
}
