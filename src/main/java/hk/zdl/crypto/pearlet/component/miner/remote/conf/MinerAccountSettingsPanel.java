package hk.zdl.crypto.pearlet.component.miner.remote.conf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.json.JSONArray;
import org.json.JSONObject;

import com.csvreader.CsvReader;

import hk.zdl.crypto.pearlet.component.miner.remote.MinerGridTitleFont;
import hk.zdl.crypto.pearlet.ds.RoturaAddress;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.crypto.SignumCrypto;

public class MinerAccountSettingsPanel extends JPanel {

	public static final String miner_account_path = "/api/v1/miner/configure/account";
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	private static final long serialVersionUID = -1698208979389357636L;
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);

	private final JList<String> acc_list = new JList<>();
	private final JButton add_btn = new JButton(rsc_bdl.getString("MINING.REMOTE.ACCOUNT.ADD"));
	private final JButton del_btn = new JButton(rsc_bdl.getString("MINING.REMOTE.ACCOUNT.DEL"));
	private HttpClient client = HttpClient.newHttpClient();
	private String basePath = "";

	public MinerAccountSettingsPanel() {
		super(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), rsc_bdl.getString("MINING.REMOTE.ACCOUNT.TITLE"), TitledBorder.CENTER, TitledBorder.TOP, MinerGridTitleFont.getFont()));
		add(new JScrollPane(acc_list), BorderLayout.CENTER);

		var btn_panel = new JPanel(new GridBagLayout());
		btn_panel.add(add_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		btn_panel.add(del_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));

		var panel_1 = new JPanel(new FlowLayout(1, 0, 0));
		panel_1.add(btn_panel);
		add(panel_1, BorderLayout.EAST);

		add_btn.addActionListener(e -> Util.submit(() -> {
			if (UIUtil.isAltDown(e)) {
				batch_import(this);
			} else {
				if (add_account()) {
					refresh_list();
				}
			}
			return null;
		}));
		del_btn.addActionListener(e -> Util.submit(() -> {
			if (del_account()) {
				refresh_list();
			}
			return null;
		}));
		acc_list.setCellRenderer(new DefaultListCellRenderer() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1165392686465658238L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				var adr = RoturaAddress.fromEither(value.toString());
				var show_numberic = Util.getUserSettings().getBoolean("show_numberic_id", false);
				if (show_numberic) {
					value = adr.getID();
				} else {
					value = adr.getFullAddress();
				}
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				return this;
			}
		});
		acc_list.setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));

		acc_list.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					del_btn.doClick();
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

	public void addListSelectionListener(ListSelectionListener listener) {
		acc_list.addListSelectionListener(listener);
	}

	@SuppressWarnings("unchecked")
	public void refresh_list() throws Exception {
		if (basePath.isBlank()) {
			return;
		}
		var request = HttpRequest.newBuilder().GET().uri(new URI(basePath + miner_account_path)).build();
		var response = client.send(request, BodyHandlers.ofString());
		if (response.statusCode() == 200) {
			var l = new JSONArray(response.body()).toList().stream().map(String::valueOf).toList();
			var str = acc_list.getSelectedValue();
			acc_list.setModel(new ListComboBoxModel<String>(l));
			if (str != null) {
				if (l.contains(str)) {
					acc_list.setSelectedValue(str, false);
				}
			}
		}
	}

	public boolean add_account() {
		var icon = UIUtil.getStretchIcon("icon/" + "wallet_2.svg", 64, 64);
		var txt_field = new JTextField(30);
		int i = JOptionPane.showConfirmDialog(getRootPane(), txt_field, rsc_bdl.getString("MINING.REMOTE.ACCOUNT.ENTER_PHRASE"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
		if (i == JOptionPane.OK_OPTION) {
			var phrase = txt_field.getText().trim();
			if (phrase.isBlank()) {
				JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("MINING.REMOTE.ACCOUNT.MSG.ERR.EMPTY"), rsc_bdl.getString("GENERAL.ERROR"), JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				try {
					do_add(phrase);
				} catch (Exception x) {
					JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
					return false;
				}

			}
		}
		return true;
	}

	private boolean do_add(String phrase) throws Exception {
		var id = SignumCrypto.getInstance().getAddressFromPassphrase(phrase).getID();
		var jobj = new JSONObject();
		jobj.put("id", id);
		jobj.put("passphrase", phrase);
		var request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(jobj.toString(), Charset.defaultCharset())).uri(new URI(basePath + miner_account_path + "/add"))
				.header("Content-type", "application/json").build();
		var response = client.send(request, BodyHandlers.ofString());
		if (response.statusCode() == 200) {
			return Integer.parseInt(response.body()) > 0;
		} else {
			throw new IllegalArgumentException(response.body());
		}
	}

	public boolean del_account() {
		if (acc_list.getSelectedIndex() < 0) {
			return false;
		}
		int i = JOptionPane.showConfirmDialog(getRootPane(), rsc_bdl.getString("MINING.REMOTE.ACCOUNT.CONFRIM_DEL"), "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == JOptionPane.YES_OPTION) {
			try {
				var id = acc_list.getSelectedValue();
				var jobj = new JSONObject();
				jobj.put("id", id);
				var request = HttpRequest.newBuilder().POST(BodyPublishers.ofString(jobj.toString(), Charset.defaultCharset())).uri(new URI(basePath + miner_account_path + "/del"))
						.header("Content-type", "application/json").build();
				var response = client.send(request, BodyHandlers.ofString());
				if (response.statusCode() != 200) {
					throw new IllegalArgumentException(response.body());
				}
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	private void batch_import(Component c) {
		var w = SwingUtilities.getWindowAncestor(c);
		var file_dialog = new JFileChooser();
		file_dialog.setDialogType(JFileChooser.OPEN_DIALOG);
		file_dialog.setMultiSelectionEnabled(false);
		file_dialog.setDragEnabled(false);
		file_dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		file_dialog.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return rsc_bdl.getString("GENERAL.CSV_FILES");
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
			}
		});
		int i = file_dialog.showOpenDialog(w);
		if (i != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = file_dialog.getSelectedFile();
		if (file == null) {
			return;
		}

		try {
			var reader = new CsvReader(Files.newBufferedReader(file.toPath()), ',');
			reader.readHeaders();
			int header_index = reader.getIndex("phrase"), total = 0, imported = 0;
			while (reader.readRecord()) {
				var phrase = reader.get(header_index);
				total++;
				try {
					if (do_add(phrase)) {
						imported++;
					}
				} catch (Exception x) {
					continue;
				}
			}
			reader.close();
			var panel = new JPanel(new GridBagLayout());
			var label_1 = new JLabel(rsc_bdl.getString("SETTINGS.ACCOUNT.IMPORT.IMPORT_COUNT"));
			panel.add(label_1, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
			var label_2 = new JLabel("" + imported);
			label_2.setHorizontalAlignment(SwingConstants.RIGHT);
			label_2.setHorizontalTextPosition(SwingConstants.RIGHT);
			panel.add(label_2, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
			var label_3 = new JLabel(rsc_bdl.getString("SETTINGS.ACCOUNT.IMPORT.IMPORT_TOTAL"));
			panel.add(label_3, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets_5, 0, 0));
			var label_4 = new JLabel("" + total);
			label_4.setHorizontalAlignment(SwingConstants.RIGHT);
			label_4.setHorizontalTextPosition(SwingConstants.RIGHT);
			panel.add(label_4, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
			JOptionPane.showMessageDialog(w, panel, rsc_bdl.getString("GENERAL.DONE"), JOptionPane.INFORMATION_MESSAGE);
		} catch (Throwable x) {
			while (x.getCause() != null) {
				x = x.getCause();
			}
			JOptionPane.showMessageDialog(w, x.getMessage(), x.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}

}
