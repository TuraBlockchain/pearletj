package hk.zdl.crypto.pearlet.component.plot;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.jar.JarFile;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jakewharton.byteunits.BinaryByteUnit;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.ui.CloseableTabbedPaneLayerUI;
import hk.zdl.crypto.pearlet.ui.ProgressBarTableCellRenderer;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.SignumAddress;

public class PlotPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 3756771655055487175L;
	private static final long byte_per_nounce = 262144;
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private final JTextField jar_file_field = new JTextField(50);
	private final JTextField plot_path_field = new JTextField(50);
	private final JSpinner pcs_spinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
	private final JSpinner fz_spinner = new JSpinner(new SpinnerNumberModel(100, 1, 1024, 1));
	private final JComboBox<String> fz_op = new JComboBox<>(new String[] { "MB", "GB" });
	private final JButton plot_btn = new JButton("Plot");
	private final JTabbedPane tabbed_pane = new JTabbedPane(JTabbedPane.SCROLL_TAB_LAYOUT);
	private CrptoNetworks network;
	private String account;
	private Path jar_path, plot_path;

	public PlotPanel() {
		super(new GridBagLayout());
		EventBus.getDefault().register(this);
		var jar_file_label = new JLabel("JAR File:");
		var plot_path_label = new JLabel("Plot Path:");
		add(jar_file_label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		add(plot_path_label, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		jar_file_field.setEditable(false);
		var jar_file_btn = new JButton("Browse...");
		add(jar_file_field, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		add(jar_file_btn, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets_5, 0, 0));

		plot_path_field.setEditable(false);
		var plot_path_btn = new JButton("Browse...");
		add(plot_path_field, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		add(plot_path_btn, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets_5, 0, 0));

		var fz_label = new JLabel("File Size:");
		var fc_label = new JLabel("File Count:");
		add(fz_label, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		add(fc_label, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));

		add(fz_spinner, new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		add(fz_op, new GridBagConstraints(5, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		add(pcs_spinner, new GridBagConstraints(4, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		add(plot_btn, new GridBagConstraints(5, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		add(new JLayer<JTabbedPane>(tabbed_pane, new CloseableTabbedPaneLayerUI()), new GridBagConstraints(0, 2, 6, 2, 2, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets_5, 0, 0));

		jar_file_btn.addActionListener(e -> check_and_set_jar_path());
		plot_path_btn.addActionListener(e -> {
			var file_dialog = new JFileChooser();
			file_dialog.setDialogType(JFileChooser.SAVE_DIALOG);
			file_dialog.setMultiSelectionEnabled(false);
			file_dialog.setDragEnabled(false);
			file_dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int i = file_dialog.showOpenDialog(getRootPane());
			if (i == JFileChooser.APPROVE_OPTION) {
				plot_path = file_dialog.getSelectedFile().toPath();
				plot_path_field.setText(plot_path.toFile().getAbsolutePath());
			}
		});
		plot_btn.addActionListener(this);
	}

	private void check_and_set_jar_path() {
		var file_dialog = new JFileChooser();
		file_dialog.setDialogType(JFileChooser.OPEN_DIALOG);
		file_dialog.setMultiSelectionEnabled(false);
		file_dialog.setDragEnabled(false);
		file_dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		file_dialog.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "JAR Files";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".jar");
			}
		});
		int i = file_dialog.showOpenDialog(getRootPane());
		if (i != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = file_dialog.getSelectedFile();
		try {
			JarFile jar_file = new JarFile(file);
			var clz = jar_file.getManifest().getMainAttributes().getValue("Main-Class");
			jar_file.close();
			if (clz == null) {
				throw new IOException("Selected JAR is not runnable!");
			} else {
				jar_path = file.toPath();
				jar_file_field.setText(file.getAbsolutePath());
			}
		} catch (IOException x) {
			JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}

	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		this.network = e.network;
		this.account = e.account;
		plot_btn.setEnabled(false);
		if (Arrays.asList(CrptoNetworks.SIGNUM, CrptoNetworks.ROTURA).contains(network)) {
			if (account != null) {
				plot_btn.setEnabled(true);
			}
		}
	}

	@SuppressWarnings("serial")
	@Override
	public void actionPerformed(ActionEvent e) {
		if (jar_path == null) {
			JOptionPane.showMessageDialog(getRootPane(), "JAR file not specified!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} else if (plot_path == null) {
			JOptionPane.showMessageDialog(getRootPane(), "Plot path not specified!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		long l = (Integer) fz_spinner.getValue();
		if (fz_op.getSelectedItem().equals("MB")) {
			l = BinaryByteUnit.MEBIBYTES.toBytes(l);
		} else if (fz_op.getSelectedItem().equals("GB")) {
			l = BinaryByteUnit.GIBIBYTES.toBytes(l);
		}
		l = l / byte_per_nounce;

		var count = (Integer) pcs_spinner.getValue();
		var jobj = new JSONObject();
		var id = SignumAddress.fromRs(account).getID();
		jobj.put("id", id);
		jobj.put("path", plot_path.toFile().getAbsolutePath());
		jobj.put("count", count);
		jobj.put("nounce", l);
		jobj.put("exitOnDone", true);

		var str = Base64.getEncoder().encodeToString(jobj.toString().getBytes());

		Util.submit(() -> {
			var table = new JTable(new DefaultTableModel(new Object[][] {}, new Object[] { "No.", "Progress" })) {

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}

			};
			var rend = new ProgressBarTableCellRenderer();
			rend.setPreferredSize(new Dimension(500, 20));
			table.getColumnModel().getColumn(1).setCellRenderer(rend);
			table.setFillsViewportHeight(true);
			table.getTableHeader().setReorderingAllowed(false);
			table.getTableHeader().setResizingAllowed(false);
			table.setColumnSelectionAllowed(false);
			table.setRowSelectionAllowed(false);
			table.setCellSelectionEnabled(false);
			table.setDragEnabled(false);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setShowGrid(true);
			var model = (DefaultTableModel) table.getModel();
			model.setRowCount(count);
			for (int i = 0; i < count; i++) {
				model.setValueAt(i + 1, i, 0);
				model.setValueAt(0.0F, i, 1);
			}
			SwingUtilities.invokeLater(() -> UIUtil.adjust_table_width(table, table.getColumnModel()));
			tabbed_pane.add(id, new JScrollPane(table));
			try {
				var proc = new ProcessBuilder("java", "-jar", jar_path.toFile().getAbsolutePath(), str).start();
				var in = proc.getInputStream();
				var reader = new BufferedReader(new InputStreamReader(in));
				while (true) {
					var line = reader.readLine();
					if (line == null) {
						break;
					}
					var o = new JSONObject(new JSONTokener(line));
					var i = o.getInt("index");
					var p = o.getFloat("progress");
					model.setValueAt(p, i, 1);
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		});
	}

}
