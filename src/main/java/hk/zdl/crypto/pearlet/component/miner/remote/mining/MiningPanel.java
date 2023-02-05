package hk.zdl.crypto.pearlet.component.miner.remote.mining;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.formdev.flatlaf.util.SystemInfo;

import hk.zdl.crypto.pearlet.component.miner.remote.conf.MinerAccountSettingsPanel;
import hk.zdl.crypto.pearlet.component.miner.remote.mining.renderer.DateCellRenderer;
import hk.zdl.crypto.pearlet.component.miner.remote.mining.renderer.IDRenderer;
import hk.zdl.crypto.pearlet.component.miner.remote.mining.renderer.MinerStatusCellRenderer;
import hk.zdl.crypto.pearlet.component.miner.remote.mining.renderer.PlotDirCellRenderer;
import hk.zdl.crypto.pearlet.ds.RoturaAddress;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;

public class MiningPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 3870247981006005478L;
	public static final String addational_path = "/api/v1/miner";
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private String basePath = "";
	private final MinerStateTableModel table_model = new MinerStateTableModel();
	private final JTable table = new JTable(table_model);
	private final JButton start_btn = new JButton("Start");
	private final JButton stop_btn = new JButton("Stop");
	private final JButton restart_btn = new JButton("Restart");

	public MiningPanel() {
		super(new BorderLayout());
		init_table();
		add(new JScrollPane(table), BorderLayout.CENTER);
		var btn_panel = new JPanel(new GridBagLayout());
		btn_panel.add(start_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		btn_panel.add(stop_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		btn_panel.add(restart_btn, new GridBagConstraints(0, 2, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));

		var panel_1 = new JPanel(new FlowLayout(1, 0, 0));
		panel_1.add(btn_panel);
		add(panel_1, BorderLayout.EAST);
		start_btn.addActionListener(e -> Util.submit(() -> {
			if (start_miner()) {
				actionPerformed(null);
			}
			return null;
		}));

		stop_btn.addActionListener(e -> Util.submit(() -> {
			if (stop_miner()) {
				actionPerformed(null);
			}
			return null;
		}));
		restart_btn.addActionListener(e -> Util.submit(() -> {
			if (restart_miner()) {
				actionPerformed(null);
			}
			return null;
		}));
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
		for (var i = 0; i < table_model.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer());
		}
		IntStream.of(1, 5).forEach(i -> table.getColumnModel().getColumn(i).setCellRenderer(new DateCellRenderer()));
		table.getColumnModel().getColumn(0).setCellRenderer(new IDRenderer());
		table.getColumnModel().getColumn(10).setCellRenderer(new MinerStatusCellRenderer());
		for (var i = 0; i < table_model.getColumnCount(); i++) {
			((DefaultTableCellRenderer) table.getColumnModel().getColumn(i).getCellRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
		}
		table.getColumnModel().getColumn(4).setCellRenderer(new PlotDirCellRenderer());
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				Point point = mouseEvent.getPoint();
				int row = table.rowAtPoint(point);
				if (mouseEvent.getClickCount() == 2 && row >= 0 & row == table.getSelectedRow()) {
					if (table.getSelectedColumn() == 0) {
						var str = table.getModel().getValueAt(row, 0).toString();
						Util.viewAccountDetail(CrptoNetworks.ROTURA, str);
					} else if (table.getSelectedColumn() == 4) {
						var jarr = table.getModel().getValueAt(row, 4);
						if (jarr != null && ((JSONArray) jarr).length() == 1) {
							var path = ((JSONArray) jarr).getString(0);
							var file = new File(path);
							if (!file.exists() || !file.isDirectory()) {
								return;
							}
							if (Desktop.getDesktop().isSupported(Action.BROWSE_FILE_DIR)) {
								Desktop.getDesktop().browseFileDirectory(file);
							} else if (SystemInfo.isWindows) {
								try {
									new ProcessBuilder().command("explorer.exe", path).start();
								} catch (IOException x) {
									Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
								}
							}
						}
					}
				}
			}
		});

	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			var in = new URL(basePath + addational_path).openStream();
			var jarr = new JSONArray(new JSONTokener(in));
			in.close();
			table_model.setData(jarr);
		} catch (Exception x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
		}
		SwingUtilities.invokeLater(() -> UIUtil.adjust_table_width(table, table.getColumnModel()));
	}

	public boolean start_miner() {
		var icon = UIUtil.getStretchIcon("icon/" + "signpost-2.svg", 64, 64);
		var show_numberic = Boolean.parseBoolean(Util.getUserSettings().getProperty("show_numberic_id"));
		try {
			var options = new JSONArray(new JSONTokener(new URL(basePath + MinerAccountSettingsPanel.miner_account_path).openStream())).toList().toArray();
			if (!show_numberic) {
				for (var i = 0; i < options.length; i++) {
					var str = options[i];
					var adr = RoturaAddress.fromEither(str.toString()).getFullAddress();
					options[i] = adr;
				}
			}
			var choice = JOptionPane.showInputDialog(getRootPane(), "Choose your wallet id to start mining:", "Start Miner", JOptionPane.QUESTION_MESSAGE, icon, options, null);
			choice = RoturaAddress.fromEither(choice.toString()).getID();
			if (choice == null) {
				return false;
			} else {
				try {
					var httpclient = HttpClients.createSystem();
					var httpPost = new HttpPost(basePath + addational_path + "/start");
					var jobj = new JSONObject();
					jobj.put("id", choice);
					httpPost.setEntity(new StringEntity(jobj.toString()));
					httpPost.setHeader("Content-type", "application/json");
					var response = httpclient.execute(httpPost);
					response.close();
					if (response.getStatusLine().getStatusCode() == 200) {
						UIUtil.displayMessage("Succeed", "Miner has started.", null);
					} else {
						UIUtil.displayMessage("Failed", "Failed to start miner.", MessageType.ERROR);
					}
				} catch (Exception x) {
					JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		} catch (Exception x) {
			return false;
		}
		return true;
	}

	public boolean stop_miner() {
		if (table.getSelectedRowCount() < 1) {
			return false;
		}
		var row = table.getSelectedRow();
		int i = JOptionPane.showConfirmDialog(getRootPane(), "Are you sure to stop this miner?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == JOptionPane.YES_OPTION) {
			try {
				var httpclient = HttpClients.createSystem();
				var httpPost = new HttpPost(basePath + addational_path + "/stop");
				var jobj = new JSONObject();
				jobj.put("id", new BigInteger(table.getValueAt(row, 0).toString()));
				httpPost.setEntity(new StringEntity(jobj.toString()));
				httpPost.setHeader("Content-type", "application/json");
				var response = httpclient.execute(httpPost);
				response.close();
				if (response.getStatusLine().getStatusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Miner has stopped.", null);
				} else {
					UIUtil.displayMessage("Failed", "Failed to stop miner.", MessageType.ERROR);
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	public boolean restart_miner() {
		if (table.getSelectedRowCount() < 1) {
			return false;
		}
		var row = table.getSelectedRow();
		int i = JOptionPane.showConfirmDialog(getRootPane(), "Are you sure to restart this miner?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == JOptionPane.YES_OPTION) {
			try {
				var id = new BigInteger(table.getValueAt(row, 0).toString());
				var httpclient = HttpClients.createSystem();
				var httpPost = new HttpPost(basePath + addational_path + "/stop");
				var jobj = new JSONObject();
				jobj.put("id", id);
				httpPost.setEntity(new StringEntity(jobj.toString()));
				httpPost.setHeader("Content-type", "application/json");
				var response = httpclient.execute(httpPost);
				response.close();
				if (response.getStatusLine().getStatusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Miner has stopped.", null);
				} else {
					UIUtil.displayMessage("Failed", "Failed to stop miner.", MessageType.ERROR);
					return false;
				}
				httpPost = new HttpPost(basePath + addational_path + "/start");
				jobj = new JSONObject();
				jobj.put("id", id);
				httpPost.setEntity(new StringEntity(jobj.toString()));
				httpPost.setHeader("Content-type", "application/json");
				response = httpclient.execute(httpPost);
				response.close();
				if (response.getStatusLine().getStatusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Miner has started.", null);
				} else {
					UIUtil.displayMessage("Failed", "Failed to start miner.", MessageType.ERROR);
					return false;
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

}
