package hk.zdl.crypto.pearlet.component.plot;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.jakewharton.byteunits.BinaryByteUnit;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.component.event.PlotDoneEvent;
import hk.zdl.crypto.pearlet.component.settings.DisplaySettings;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.plot.PlotProgressListener;
import hk.zdl.crypto.pearlet.plot.PlotUtil;
import hk.zdl.crypto.pearlet.ui.CloseableTabbedPaneLayerUI;
import hk.zdl.crypto.pearlet.ui.ProgressBarTableCellRenderer;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.SignumAddress;

public class PlotPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 3756771655055487175L;
	private static final long byte_per_nounce = 262144;
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private final JSlider mem_slider = new JSlider(1, 8, 2);
	private final JLabel mem_field = new JLabel("2GiB");
	private final JTextField plot_path_field = new JTextField(100);
	private final JSpinner pcs_spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
	private final JSpinner fz_spinner = new JSpinner(new SpinnerNumberModel(50, 1, 1024, 1));
	private final JComboBox<String> fz_op = new JComboBox<>(new String[] { "MB", "GB" });
	private final JButton plot_btn = new JButton("Plot");
	private final JTabbedPane tabbed_pane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
	private CryptoNetwork network;
	private String account;
	private Path plot_path;
	private ExecutorService es;

	public PlotPanel() {
		super(new GridBagLayout());
		EventBus.getDefault().register(this);
		add(new JLabel("Plot Path:"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		add(new JLabel("Memory Limit:"), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));

		plot_path_field.setEditable(false);
		add(plot_path_field, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

		add(mem_slider, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

		var plot_path_btn = new JButton("Browse...");
		add(plot_path_btn, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets_5, 0, 0));

		mem_field.setFont(new Font(Font.MONOSPACED, Font.BOLD, getFont().getSize()));
		add(mem_field, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets_5, 0, 0));

		add(new JLabel("File Size:"), new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		add(new JLabel("File Count:"), new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));

		add(fz_spinner, new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		add(fz_op, new GridBagConstraints(5, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		add(pcs_spinner, new GridBagConstraints(4, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		add(plot_btn, new GridBagConstraints(5, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
		add(new JLayer<JTabbedPane>(tabbed_pane, new CloseableTabbedPaneLayerUI()), new GridBagConstraints(0, 2, 6, 2, 2, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets_5, 0, 0));
		fz_op.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
		fz_op.getModel().setSelectedItem("GB");

		mem_slider.addChangeListener(e -> {
			mem_field.setText(mem_slider.getValue() + "GiB");
		});
		plot_path_btn.addActionListener(e -> {
			var file_dialog = new JFileChooser();
			file_dialog.setDialogType(JFileChooser.SAVE_DIALOG);
			file_dialog.setMultiSelectionEnabled(false);
			file_dialog.setDragEnabled(false);
			file_dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int i = file_dialog.showOpenDialog(getRootPane());
			if (i == JFileChooser.APPROVE_OPTION) {
				var f = file_dialog.getSelectedFile();
				plot_path = f.toPath();
				plot_path_field.setText(f.getAbsolutePath());
			}
		});
		plot_btn.addActionListener(this);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		this.network = e.network;
		this.account = e.account;
		plot_btn.setEnabled(false);
		if (network != null && network.isBurst()) {
			if (account != null) {
				plot_btn.setEnabled(true);
			}
		}
	}

	@SuppressWarnings("serial")
	@Override
	public void actionPerformed(ActionEvent e) {
		if (plot_path == null) {
			JOptionPane.showMessageDialog(getRootPane(), "Plot path not specified!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			for (char c : plot_path.toAbsolutePath().toString().toCharArray()) {
				if (c > 127) {
					JOptionPane.showMessageDialog(getRootPane(), "Path should contain only ASCII characters!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		}
		var plot_path_str = plot_path.toFile().getAbsolutePath();

		long l = (Integer) fz_spinner.getValue();
		if (fz_op.getSelectedItem().equals("MB")) {
			l = BinaryByteUnit.MEBIBYTES.toBytes(l);
		} else if (fz_op.getSelectedItem().equals("GB")) {
			l = BinaryByteUnit.GIBIBYTES.toBytes(l);
		}
		var file_size = l;
		var nounces = l / byte_per_nounce;

		if (nounces < 10) {
			JOptionPane.showMessageDialog(getRootPane(), "File size too small!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		var count = (Integer) pcs_spinner.getValue();
		var id = SignumAddress.fromRs(account).getID();

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
			synchronized (tabbed_pane) {
				if (es == null) {
					es = Executors.newSingleThreadExecutor(r -> {
						var t = new Thread(r, PlotPanel.class.getSimpleName());
						t.setDaemon(true);
						t.setPriority(Thread.MIN_PRIORITY);
						return t;
					});
				}
			}
			es.submit(() -> {
				for (var i = 0; i < count; i++) {
					var _i = i;
					try {
						PlotUtil.do_plot(Paths.get(plot_path_str), id, nounces, new PlotProgressListener() {

							@Override
							public void onProgress(Type type, float progress, String rate, String ETA) {
								if (type == Type.WRIT) {
									model.setValueAt(progress, _i, 1);
									if (progress >= 100) {
										on_plot_done(plot_path, file_size, id);
									}
								}
							}
						}, mem_field.getText());
					} catch (Exception x) {
						model.setValueAt(-1F, _i, 1);
						UIUtil.displayMessage(x.getClass().getSimpleName(), x.getMessage(), MessageType.ERROR);
					}
				}
			});
		});
	}

	private static final void on_plot_done(Path path, long fz, String id) {
		EventBus.getDefault().post(new PlotDoneEvent(path));
		var perf = Util.getUserSettings();
		if (perf.getBoolean(DisplaySettings.SNPF, false)) {
			var msg = "File Size: ";
			if (fz > BinaryByteUnit.GIBIBYTES.toBytes(1)) {
				msg += BinaryByteUnit.BYTES.toGibibytes(fz) + "GB";
			} else {
				msg += BinaryByteUnit.BYTES.toMebibytes(fz) + "MB";
			}
			msg += "\nID: ";
			if (perf.getBoolean(DisplaySettings.SNID, false)) {
				msg += id;
			} else {
				msg += SignumAddress.fromEither(id).getRawAddress();
			}
			UIUtil.displayMessage("Plot Done!", msg, MessageType.INFO);
		}
	}
}
