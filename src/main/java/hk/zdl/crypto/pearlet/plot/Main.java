package hk.zdl.crypto.pearlet.plot;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Taskbar;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Random;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import com.jakewharton.byteunits.BinaryByteUnit;
import com.jthemedetecor.OsThemeDetector;

public class Main {

	private static final long byte_per_nounce = 262144;
	private static boolean show_dialog_on_done = true;

	@SuppressWarnings("serial")
	public static void main(String[] args) throws Throwable {
		System.setProperty("apple.awt.application.appearance", "system");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		var otd = OsThemeDetector.getDetector();
		UIManager.setLookAndFeel(otd.isDark() ? new FlatDarkLaf() : new FlatLightLaf());
		var app_icon = ImageIO.read(Main.class.getClassLoader().getResource("app_icon.png"));
		try {
			Taskbar.getTaskbar().setIconImage(app_icon);
		} catch (Exception x) {
		}
		var frame = new JFrame("Tura plot");
		frame.setIconImage(app_icon);
		var layout = new CardLayout();
		frame.setLayout(layout);
		var option_pane = new JPanel();
		frame.add(option_pane, "option_pane");

		option_pane.setLayout(new GridBagLayout());
		var id_label = new JLabel("Wallet ID");
		option_pane.add(id_label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		var id_field = new JTextField("1234567890", 100);
		option_pane.add(id_field, new GridBagConstraints(1, 0, 2, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		var path_label = new JLabel("Path");
		option_pane.add(path_label, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		var path_field = new JTextField(100);
		path_field.setText(System.getProperty("user.home"));
		path_field.setEditable(false);
		option_pane.add(path_field, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		var path_btn = new JButton("Select...");

		option_pane.add(path_btn, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		var fz_label = new JLabel("File Size");
		option_pane.add(fz_label, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		var fz_spinner = new JSpinner(new SpinnerNumberModel(100, 1, 1024, 1));
		option_pane.add(fz_spinner, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		var fz_op = new JComboBox<>(new String[] { "MB", "GB" });
		option_pane.add(fz_op, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		var start_btn = new JButton("Start");
		start_btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 100));
		option_pane.add(start_btn, new GridBagConstraints(0, 4, 3, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

		layout.show(frame.getContentPane(), "option_pane");
		var progress_pane = new PlotProgressPanel() {

			@Override
			public void onProgress(Type type, float progress, String rate, String ETA) {
				super.onProgress(type, progress, rate, ETA);
				if (type == Type.WRIT) {
					try {
						if (progress >= 100) {
							Taskbar.getTaskbar().setWindowProgressValue(frame, 0);
							Taskbar.getTaskbar().setWindowProgressState(frame, Taskbar.State.OFF);
						} else {
							Taskbar.getTaskbar().setWindowProgressState(frame, Taskbar.State.NORMAL);
							Taskbar.getTaskbar().setWindowProgressValue(frame, (int) progress);
						}
					} catch (Exception x) {
					}
				}
			}

			@Override
			public void onDone() {
				if (show_dialog_on_done) {
					JOptionPane.showMessageDialog(getRootPane(), "Plot Finish!", "Done", JOptionPane.INFORMATION_MESSAGE);
				}
				setDone(false);
			}
		};
		frame.add(progress_pane, "progress_pane");
		var size = new Dimension(640, 480);
		frame.setPreferredSize(size);
		frame.setMinimumSize(size);
		frame.setResizable(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		otd.registerListener(isDark -> {
			Stream.of(new FlatLightLaf(), new FlatDarkLaf()).filter(o -> o.isDark() == isDark).forEach(FlatLaf::setup);
			SwingUtilities.invokeLater(() -> {
				SwingUtilities.updateComponentTreeUI(frame);
			});
		});

		progress_pane.setDone(false);

		path_btn.addActionListener(e -> {
			var file_dialog = new JFileChooser();
			file_dialog.setDialogType(JFileChooser.SAVE_DIALOG);
			file_dialog.setMultiSelectionEnabled(false);
			file_dialog.setDragEnabled(false);
			file_dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int i = file_dialog.showOpenDialog(frame);
			if (i == JFileChooser.APPROVE_OPTION) {
				path_field.setText(file_dialog.getSelectedFile().getAbsolutePath());
			}
		});
		start_btn.addActionListener(e -> {
			var id = id_field.getText().trim().replace("+", "").replace("-", "");
			try {
				Long.parseUnsignedLong(id);
			} catch (NumberFormatException x) {
				JOptionPane.showMessageDialog(frame, "Invalid Wallet ID!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			var dir = new File(path_field.getText());
			if (!dir.exists()) {
				JOptionPane.showMessageDialog(frame, "Path not exist!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			} else if (!dir.isDirectory()) {
				JOptionPane.showMessageDialog(frame, "Path is not Directory!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			} else if (!dir.canWrite()) {
				JOptionPane.showMessageDialog(frame, "Path is not Writable!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				for (char c : dir.getAbsolutePath().toCharArray()) {
					if (c > 127) {
						JOptionPane.showMessageDialog(frame, "Path should contain only ASCII characters!", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}

			new Thread() {

				@Override
				public void run() {

					long l = (Integer) fz_spinner.getValue();
					if (fz_op.getSelectedItem().equals("MB")) {
						l = BinaryByteUnit.MEBIBYTES.toBytes(l);
					} else if (fz_op.getSelectedItem().equals("GB")) {
						l = BinaryByteUnit.GIBIBYTES.toBytes(l);
					}
					l = l / byte_per_nounce;

					start_btn.setEnabled(false);
					try {
						layout.show(frame.getContentPane(), "progress_pane");
						do_plot(dir.toPath(), id, l, progress_pane);
					} catch (Exception x) {
						JOptionPane.showMessageDialog(frame, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					} finally {
						layout.show(frame.getContentPane(), "option_pane");
						start_btn.setEnabled(true);
					}
				}
			}.start();
		});
		if (args.length > 0) {
			byte[] bArr = Base64.getDecoder().decode(args[0]);
			var jobj = new JSONObject(new JSONTokener(new InputStreamReader(new ByteArrayInputStream(bArr), Charset.forName("UTF-8"))));
			var id = jobj.getString("id");
			var path = Paths.get(jobj.getString("path"));
			var nounce = jobj.getLong("nounce");
			var count = jobj.optInt("count", 1);

			layout.show(frame.getContentPane(), "progress_pane");
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			show_dialog_on_done = false;
			int[] a = new int[1];
			var listener = new PlotProgressListener() {

				@Override
				public void onProgress(Type type, float progress, String rate, String ETA) {
					if (type == Type.WRIT) {
						var jobj = new JSONObject();
						jobj.put("index", a[0]);
						jobj.put("progress", progress);
						System.out.println(jobj);
					}
					progress_pane.onProgress(type, progress, rate, ETA);
				}

			};
			try {
				for (a[0] = 0; a[0] < count; a[0]++) {
					do_plot(path, id, nounce, listener);
				}
			} catch (Exception x) {
				x.printStackTrace(System.err);
				JOptionPane.showMessageDialog(frame, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} finally {
				layout.show(frame.getContentPane(), "option_pane");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				show_dialog_on_done = true;
				start_btn.setEnabled(true);
			}
			if (jobj.optBoolean("exitOnDone")) {
				System.exit(0);
			}
		}

	}

	public static void do_plot(Path dir, String id, long nounce, PlotProgressListener listener, String... mem_usage) throws Exception {
		Path plotter_bin_path = copy_plotter().toPath();
		Process proc = PlotUtil.plot(plotter_bin_path, dir, false, new BigInteger(id), Math.abs(new Random().nextInt()), nounce, listener);
		int i = proc.waitFor();
		if (i != 0) {
			var err_info = IOUtils.readLines(proc.getErrorStream(), Charset.defaultCharset()).stream().reduce("", (a, b) -> a + "\n" + b).trim();
			throw new IOException(err_info);
		}
		Files.deleteIfExists(plotter_bin_path);
	}

	private static File copy_plotter() throws IOException {
		String suffix = "";
		if (SystemInfo.isWindows) {
			suffix = ".exe";
		}
		File tmp_file = File.createTempFile("plotter-", suffix);
		tmp_file.deleteOnExit();
		String in_filename = "";
		if (SystemInfo.isLinux) {
			in_filename = "signum-plotter";
		} else if (SystemInfo.isWindows) {
			in_filename = "signum-plotter.exe";
		} else if (SystemInfo.isMacOS) {
			in_filename = "signum-plotter-x86_64-apple-darwin.zip";
		}
		InputStream in = Main.class.getClassLoader().getResourceAsStream("plotter/" + in_filename);
		FileOutputStream out = new FileOutputStream(tmp_file);
		IOUtils.copy(in, out);
		out.flush();
		out.close();
		in.close();
		if (SystemInfo.isMacOS) {
			ZipFile zipfile = new ZipFile(tmp_file);
			ZipEntry entry = zipfile.stream().findAny().get();
			in = zipfile.getInputStream(entry);
			tmp_file = File.createTempFile("plotter-", ".app");
			tmp_file.deleteOnExit();
			out = new FileOutputStream(tmp_file);
			IOUtils.copy(in, out);
			out.flush();
			out.close();
			in.close();
			zipfile.close();
		}
		tmp_file.setExecutable(true);
		return tmp_file;
	}

}
