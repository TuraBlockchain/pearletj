package hk.zdl.crypto.pearlet.component.miner.remote.mining;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
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

import org.json.JSONArray;
import org.json.JSONObject;

import com.vaadin.open.Open;

import hk.zdl.crypto.pearlet.component.miner.remote.conf.MinerAccountSettingsPanel;
import hk.zdl.crypto.pearlet.component.miner.remote.mining.renderer.DateCellRenderer;
import hk.zdl.crypto.pearlet.component.miner.remote.mining.renderer.IDRenderer;
import hk.zdl.crypto.pearlet.component.miner.remote.mining.renderer.MinerStatusCellRenderer;
import hk.zdl.crypto.pearlet.component.miner.remote.mining.renderer.PlotDirCellRenderer;
import hk.zdl.crypto.pearlet.ds.RoturaAddress;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class MiningPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 3870247981006005478L;
	public static final String addational_path = "/api/v1/miner";
	public static final String miner_reboot_path = "/api/v1/miner/restart_all";
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private final MinerStateTableModel table_model = new MinerStateTableModel();
	private final JTable table = new JTable(table_model);
	private final JButton start_btn = new JButton("Start");
	private final JButton stop_btn = new JButton("Stop");
	private final JButton restart_btn = new JButton("Restart");
	private final JButton restart_all_btn = new JButton("Restart All");
	private HttpClient client = HttpClient.newHttpClient();
	private String basePath = "";

	public MiningPanel() {
		super(new BorderLayout());
		init_table();
		add(new JScrollPane(table), BorderLayout.CENTER);
		var btn_panel = new JPanel(new GridBagLayout());
		btn_panel.add(start_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(stop_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(restart_btn, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(restart_all_btn, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

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
		restart_all_btn.addActionListener(e -> Util.submit(() -> {
			if (restart_all_miner()) {
				actionPerformed(null);
			}
			return null;
		}));

		table.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					stop_btn.doClick();
				}
			}
		});
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
		for (var i = 0; i < table_model.getColumnCount(); i++) {
			((DefaultTableCellRenderer) table.getColumnModel().getColumn(i).getCellRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
		}
		table.getColumnModel().getColumn(4).setCellRenderer(new PlotDirCellRenderer());
		table.getColumnModel().getColumn(10).setCellRenderer(new MinerStatusCellRenderer());
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				Point point = mouseEvent.getPoint();
				int row = table.rowAtPoint(point);
				if (mouseEvent.getClickCount() == 2 && row >= 0 & row == table.getSelectedRow()) {
					if (table.getSelectedColumn() == 0) {
						var nws = MyDb.get_networks();
						if (nws.size() == 1) {
							try {
								var str = table.getModel().getValueAt(row, 0).toString();
								Util.viewAccountDetail(nws.get(0), str);
							} catch (Exception e) {
							}
						}
					} else if (table.getSelectedColumn() == 4) {
						var jarr = table.getModel().getValueAt(row, 4);
						if (jarr != null && ((JSONArray) jarr).length() == 1) {
							var path = ((JSONArray) jarr).getString(0);
							var file = new File(path);
							if (!file.exists() || !file.isDirectory()) {
								return;
							}
							Open.open(file.getAbsolutePath());
						}
					}
				}
			}
		});

	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			var request = HttpRequest.newBuilder().GET().uri(new URI(basePath + addational_path)).build();
			var response = client.send(request, BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				var jarr = new JSONArray(response.body());
				table_model.setData(jarr);
			}
		} catch (Exception x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
		}
		SwingUtilities.invokeLater(() -> UIUtil.adjust_table_width(table, table.getColumnModel()));
	}

	public boolean start_miner() {
		var icon = UIUtil.getStretchIcon("icon/" + "signpost-2.svg", 64, 64);
		var show_numberic = Util.getUserSettings().getBoolean("show_numberic_id", false);
		try {
			var request = HttpRequest.newBuilder().GET().uri(new URI(basePath + MinerAccountSettingsPanel.miner_account_path)).build();
			var response = client.send(request, BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				return false;
			}
			var options = new JSONArray(response.body()).toList().toArray();
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
					var jobj = new JSONObject();
					jobj.put("id", choice);
					request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(jobj.toString(), Charset.defaultCharset())).uri(new URI(basePath + addational_path + "/start"))
							.header("Content-type", "application/json").build();
					response = client.send(request, BodyHandlers.ofString());
					if (response.statusCode() == 200) {
						UIUtil.displayMessage("Succeed", "Miner has started.");
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
				var jobj = new JSONObject();
				jobj.put("id", new BigInteger(table.getValueAt(row, 0).toString()));
				var request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(jobj.toString(), Charset.defaultCharset())).uri(new URI(basePath + addational_path + "/stop"))
						.header("Content-type", "application/json").build();
				var response = client.send(request, BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Miner has stopped.");
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
				var jobj = new JSONObject();
				jobj.put("id", id);
				var request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(jobj.toString(), Charset.defaultCharset())).uri(new URI(basePath + addational_path + "/stop"))
						.header("Content-type", "application/json").build();
				var response = client.send(request, BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Miner has stopped.");
				} else {
					UIUtil.displayMessage("Failed", "Failed to stop miner.", MessageType.ERROR);
					return false;
				}
				jobj = new JSONObject();
				jobj.put("id", id);
				request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(jobj.toString(), Charset.defaultCharset())).uri(new URI(basePath + addational_path + "/start"))
						.header("Content-type", "application/json").build();
				response = client.send(request, BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Miner has started.");
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

	public boolean restart_all_miner() {
		int i = JOptionPane.showConfirmDialog(getRootPane(), "Are you sure to restart all miners?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == JOptionPane.YES_OPTION) {
			try {
				var request = HttpRequest.newBuilder().POST(BodyPublishers.noBody()).uri(new URI(basePath + miner_reboot_path)).header("Content-type", "application/json").build();
				var response = client.send(request, BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					UIUtil.displayMessage("Succeed", "Miners have restarted.");
				} else {
					UIUtil.displayMessage("Failed", "Failed to restart miners.", MessageType.ERROR);
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
