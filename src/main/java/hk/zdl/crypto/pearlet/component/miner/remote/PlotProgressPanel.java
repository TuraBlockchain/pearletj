package hk.zdl.crypto.pearlet.component.miner.remote;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jakewharton.byteunits.BinaryByteUnit;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.component.miner.remote.conf.MinerAccountSettingsPanel;
import hk.zdl.crypto.pearlet.component.miner.remote.conf.MinerPathSettingPanel;
import hk.zdl.crypto.pearlet.ds.RoturaAddress;
import hk.zdl.crypto.pearlet.ui.ProgressBarTableCellRenderer;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class PlotProgressPanel extends JPanel {

	public static final String plot_path = "/api/v1/plot";
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	private static final long serialVersionUID = -1150055243038748734L;
	private static final long byte_per_nounce = 262144;
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private final JButton add_btn = new JButton(rsc_bdl.getString("MINER.REMOTE.PLOT.ADD"));
	private final JButton del_btn = new JButton(rsc_bdl.getString("MINER.REMOTE.PLOT.DEL"));
	private final JButton clear_btn = new JButton(rsc_bdl.getString("MINER.REMOTE.PLOT.CLEAR"));
	private final Object[] col_name = Stream.of("TYPE", "RATE", "PROG", "ETA", "PATH").map(s -> rsc_bdl.getString("MINER.REMOTE.PLOT.TABLE.COLUMN_NAME." + s)).toArray();
	private final JTable table = new JTable(new DefaultTableModel(new Object[][] {}, col_name)) {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7710119472375311686L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	private HttpClient client = HttpClient.newHttpClient();
	private String basePath = "";
	private String selected_account_id = "";

	public PlotProgressPanel() {
		super(new BorderLayout());
		init_table();
		add(new JScrollPane(table), BorderLayout.CENTER);
		var btn_panel = new JPanel(new GridBagLayout());
		btn_panel.add(add_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(del_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(clear_btn, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var panel_1 = new JPanel(new FlowLayout(1, 0, 0));
		panel_1.add(btn_panel);
		add(panel_1, BorderLayout.EAST);
		add_btn.addActionListener(e -> Util.submit(this::addPlot));
		del_btn.addActionListener(e -> Util.submit(this::delPlot));
		clear_btn.addActionListener(e -> Util.submit(this::clear_done));
		EventBus.getDefault().register(this);
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void setClient(HttpClient client) {
		this.client = client;
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

	@SuppressWarnings("unchecked")
	private final void addPlot() {
		var panel = new JPanel(new GridBagLayout());
		var label_1 = new JLabel("id:");
		panel.add(label_1, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		var combo_box_1 = new JComboBox<String>();
		combo_box_1.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
		panel.add(combo_box_1, new GridBagConstraints(1, 0, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var label_2 = new JLabel(rsc_bdl.getString("PLOT.PATH_LABEL"));
		panel.add(label_2, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		var combo_box_2 = new JComboBox<String>();
		panel.add(combo_box_2, new GridBagConstraints(1, 1, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var label_4 = new JLabel(rsc_bdl.getString("PLOT.FILE_SIZE"));
		panel.add(label_4, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		var fz_spinner = new JSpinner(new SpinnerNumberModel(50, 1, 1024, 1));
		var fz_op = new JComboBox<>(new String[] { "MB", "GB" });
		panel.add(fz_spinner, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		panel.add(fz_op, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets_5, 0, 0));
		var chech_box_1 = new JCheckBox(rsc_bdl.getString("MINER.REMOTE.PLOT.RESTART"), true);
		panel.add(chech_box_1, new GridBagConstraints(0, 3, 3, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		fz_op.getModel().setSelectedItem("GB");
		Util.submit(() -> {
			var show_numberic = Util.getUserSettings().getBoolean("show_numberic_id", false);
			var request = HttpRequest.newBuilder().GET().uri(new URI(basePath + MinerAccountSettingsPanel.miner_account_path)).build();
			var response = client.send(request, BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				var list = new JSONArray(response.body()).toList().stream().map(String::valueOf).toList();
				list = list.stream().map(RoturaAddress::fromEither).map(r -> show_numberic ? r.getID() : r.getFullAddress()).toList();
				combo_box_1.setModel(new ListComboBoxModel<String>(list));
				combo_box_1.getActionListeners()[0].actionPerformed(null);
				for (var str : list) {
					var a = RoturaAddress.fromEither(str).getFullAddress();
					if (a.equals(selected_account_id)) {
						combo_box_1.getModel().setSelectedItem(str);
						break;
					}
				}
			} else {
				return null;
			}
			return null;
		});
		combo_box_1.addActionListener(e -> {
			Util.submit(() -> {
				var str = RoturaAddress.fromEither(combo_box_1.getSelectedItem().toString()).getID();
				var request = HttpRequest.newBuilder().GET().uri(new URI(basePath + MinerPathSettingPanel.miner_file_path + "/list?id=" + str)).build();
				var response = client.send(request, BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					var jarr = new JSONArray(response.body());
					combo_box_2.setModel(new ListComboBoxModel<String>(jarr.toList().stream().map(String::valueOf).toList()));
				}
				return null;
			});
		});

		int i = JOptionPane.showConfirmDialog(getRootPane(), panel, rsc_bdl.getString("MINER.REMOTE.PLOT.ADD_TITLE"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == JOptionPane.OK_OPTION && combo_box_1.getSelectedIndex() > -1 && combo_box_2.getSelectedIndex() > -1) {
			long l = (Integer) fz_spinner.getValue();
			if (fz_op.getSelectedItem().equals("MB")) {
				l = BinaryByteUnit.MEBIBYTES.toBytes(l);
			} else if (fz_op.getSelectedItem().equals("GB")) {
				l = BinaryByteUnit.GIBIBYTES.toBytes(l);
			}
			l = l / byte_per_nounce;

			if (l < 10) {
				JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("PLOT.MSG.ERR.FILE.TOO_SMALL"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			try {
				var str = RoturaAddress.fromEither(combo_box_1.getSelectedItem().toString()).getID();
				var jobj = new JSONObject();
				jobj.put("id", new BigInteger(str));
				jobj.put("nounces", l);
				jobj.put("target_path", combo_box_2.getSelectedItem());
				jobj.put("restart", chech_box_1.isSelected());
				str = jobj.toString();
				var request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(str, Charset.defaultCharset())).uri(new URI(basePath + plot_path + "/add"))
						.header("Content-type", "application/json").build();
				var response = client.send(request, BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					UIUtil.displayMessage(rsc_bdl.getString("GENERAL.SUCCEED"), rsc_bdl.getString("MINER.REMOTE.PLOT.QUEUED"));
					Util.submit(() -> {
						for (var a = 0; a < 5; a++) {
							refresh_current_plots();
							TimeUnit.SECONDS.sleep(1);
						}
						return null;
					});
				} else {
					JOptionPane.showMessageDialog(getRootPane(), response.body(), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private final void delPlot() {
		var index = table.getSelectedRow() / 2;
		if (index < 0) {
			return;
		}
		try {
			var jobj = new JSONObject();
			jobj.put("index", index);
			var request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(jobj.toString(), Charset.defaultCharset())).uri(new URI(basePath + plot_path + "/del"))
					.header("Content-type", "application/json").build();
			var response = client.send(request, BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				UIUtil.displayMessage(rsc_bdl.getString("GENERAL.SUCCEED"), "");
				Util.submit(() -> refresh_current_plots());
			} else {
				JOptionPane.showMessageDialog(getRootPane(), response.body(), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception x) {
			JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}

	private final void clear_done() {
		try {
			var request = HttpRequest.newBuilder().POST(BodyPublishers.noBody()).uri(new URI(basePath + plot_path + "/clear_done")).build();
			var response = client.send(request, BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				UIUtil.displayMessage(rsc_bdl.getString("GENERAL.SUCCEED"), "");
				Util.submit(() -> refresh_current_plots());
			} else {
				JOptionPane.showMessageDialog(getRootPane(), response.body(), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception x) {
			JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}

	public Void refresh_current_plots() throws Exception {
		var request = HttpRequest.newBuilder().GET().uri(new URI(basePath + plot_path + "/list")).build();
		var response = client.send(request, BodyHandlers.ofString());
		var jarr = new JSONArray(response.body());
		var model = (DefaultTableModel) table.getModel();
		model.setRowCount(jarr.length() * 2);
		for (int i = 0; i < jarr.length(); i++) {
			int row_1 = i * 2;
			int row_2 = i * 2 + 1;
			model.setValueAt("Hash", row_1, 0);
			model.setValueAt("Write", row_2, 0);
			var jobj = jarr.optJSONObject(i);
			if (jobj == null) {
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
		return null;
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		if (e.account != null && e.network.isBurst()) {
			selected_account_id = e.account;
		}
	}
}
