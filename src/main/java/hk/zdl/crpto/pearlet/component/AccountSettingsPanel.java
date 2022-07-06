package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.util.Base64;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import hk.zdl.crpto.pearlet.MyToolbar;
import hk.zdl.crpto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crpto.pearlet.misc.AccountTableModel;
import hk.zdl.crpto.pearlet.misc.IndepandentWindows;
import hk.zdl.crpto.pearlet.persistence.MyDb;
import hk.zdl.crpto.pearlet.ui.UIUtil;
import hk.zdl.crpto.pearlet.util.CrptoNetworks;
import hk.zdl.crpto.pearlet.util.CryptoUtil;
import hk.zdl.crpto.pearlet.util.Util;

@SuppressWarnings("serial")
public class AccountSettingsPanel extends JPanel {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private final AccountTableModel account_table_model = new AccountTableModel();
	private final JTable table = buildAccountTable();

	public AccountSettingsPanel() {
		super(new BorderLayout());
		EventBus.getDefault().register(account_table_model);
		var scr_1 = new JScrollPane(table);
		add(scr_1, BorderLayout.CENTER);

		var btn_panel = new JPanel(new GridBagLayout());
		var create_account_btn = new JButton("Create");
		btn_panel.add(create_account_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var import_account_btn = new JButton("Import");
		btn_panel.add(import_account_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var watch_account_btn = new JButton("Watch");
		btn_panel.add(watch_account_btn, new GridBagConstraints(0, 2, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var del_btn = new JButton("Delete");
		btn_panel.add(del_btn, new GridBagConstraints(0, 3, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));

		create_account_btn.addActionListener(e -> create_new_account_dialog(this));

		import_account_btn.addActionListener(e -> create_import_account_dialog(this));

		watch_account_btn.addActionListener(e -> create_watch_account_dialog(this));
		watch_account_btn.setEnabled(false);//FIXME

		del_btn.addActionListener(e -> Util.submit(() -> {
			int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			int i = JOptionPane.showConfirmDialog(getRootPane(), "Are you sure to delete this?", "", JOptionPane.YES_NO_OPTION);
			if (i == 0) {
				int id = Integer.parseInt(account_table_model.getValueAt(row, 0).toString());
				MyDb.deleteAccount(id);
				reload_accounts();
			}
		}));

		reload_accounts();

		var panel_1 = new JPanel(new FlowLayout(1, 0, 0));
		panel_1.add(btn_panel);
		add(panel_1, BorderLayout.EAST);
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent e) {
				SwingUtilities.invokeLater(() -> UIUtil.adjust_table_width(table, table.getColumnModel()));
			}

		});
		table.getModel().addTableModelListener((e) -> SwingUtilities.invokeLater(() -> UIUtil.adjust_table_width(table, table.getColumnModel())));
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				Point point = mouseEvent.getPoint();
				int row = table.rowAtPoint(point);
				if (mouseEvent.getClickCount() == 2 && row >= 0 & row == table.getSelectedRow()) {
					CrptoNetworks nw = CrptoNetworks.valueOf(account_table_model.getValueAt(row, 1).toString());
					Util.viewAccountDetail(nw, account_table_model.getValueAt(row, 2));
				}
			}
		});

	}

	private final JTable buildAccountTable() {
		var table = new JTable(account_table_model);
		table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setShowGrid(true);
		return table;
	}

	private static final void reload_accounts() {
		Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
	}

	@SuppressWarnings("unchecked")
	private static final void create_new_account_dialog(Component c) {
		var w = SwingUtilities.getWindowAncestor(c);
		var dialog = new JDialog(w, "Create New Account", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		IndepandentWindows.add(dialog);
		var panel = new JPanel(new GridBagLayout());
		try {
			panel.add(new JLabel(new MyStretchIcon(ImageIO.read(MyToolbar.class.getClassLoader().getResource("icon/" + "cloud-plus-fill.svg")), 64, 64)),
					new GridBagConstraints(0, 0, 1, 4, 0, 0, 17, 0, insets_5, 0, 0));
		} catch (IOException e) {
		}
		var label_1 = new JLabel("Network:");
		var network_combobox = new JComboBox<>();
		network_combobox.setModel(new EnumComboBoxModel<>(CrptoNetworks.class));
		var label_2 = new JLabel("Text type:");
		var combobox_1 = new JComboBox<>(new String[] { "HEX", "Base64" });
		panel.add(label_1, new GridBagConstraints(1, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(network_combobox, new GridBagConstraints(2, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(label_2, new GridBagConstraints(3, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(combobox_1, new GridBagConstraints(4, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		var text_area = new JTextArea(5, 30);
		var scr_pane = new JScrollPane(text_area);
		scr_pane.setPreferredSize(scr_pane.getSize());
		panel.add(scr_pane, new GridBagConstraints(1, 1, 4, 3, 0, 0, 17, 1, insets_5, 0, 0));
		text_area.setEditable(false);

		var btn_1 = new JButton("Random");
		var btn_2 = new JButton("Copy");
		var btn_3 = new JButton("OK");
		var panel_1 = new JPanel(new GridBagLayout());
		panel_1.add(btn_1, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		panel_1.add(btn_2, new GridBagConstraints(1, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		panel_1.add(btn_3, new GridBagConstraints(2, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));

		panel.add(panel_1, new GridBagConstraints(0, 5, 5, 1, 0, 0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));

		btn_1.addActionListener(e -> {
			byte[] bArr = new byte[32];
			new Random().nextBytes(bArr);
			if (combobox_1.getSelectedItem().toString().equals("HEX")) {
				StringBuilder sb = new StringBuilder();
				for (byte b : bArr) {
					sb.append(String.format("%02X", b));
					sb.append(' ');
				}
				String s = sb.toString().trim();
				text_area.setText(s);
			} else if (combobox_1.getSelectedItem().toString().equals("Base64")) {
				text_area.setText(Base64.encodeBytes(bArr));
			}
		});
		btn_2.addActionListener(e -> {
			var s = new StringSelection(text_area.getText().trim());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, s);
		});
		btn_3.addActionListener(e -> Util.submit(() -> {
			CrptoNetworks nw = CrptoNetworks.valueOf(network_combobox.getSelectedItem().toString());
			String type = combobox_1.getSelectedItem().toString();
			String text = text_area.getText().trim();

			boolean b = false;
			byte[] public_key, private_key;
			try {
				private_key = CryptoUtil.getPrivateKey(nw, type, text);
				public_key = CryptoUtil.getPublicKey(nw, private_key);
				b = MyDb.insertAccount(nw, public_key, private_key);
			} catch (Exception x) {
				JOptionPane.showMessageDialog(dialog, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (b) {
				dialog.dispose();
				reload_accounts();
			} else {
				JOptionPane.showMessageDialog(dialog, "Something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}));

		dialog.add(panel);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(w);
		dialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(WindowEvent e) {
				btn_1.doClick();
			}

		});
		dialog.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	private static final void create_import_account_dialog(Component c) {
		var w = SwingUtilities.getWindowAncestor(c);
		var dialog = new JDialog(w, "Import Existing Account", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		IndepandentWindows.add(dialog);
		var panel = new JPanel(new GridBagLayout());
		try {
			panel.add(new JLabel(new MyStretchIcon(ImageIO.read(MyToolbar.class.getClassLoader().getResource("icon/" + "wallet_2.svg")), 64, 64)),
					new GridBagConstraints(0, 0, 1, 4, 0, 0, 17, 0, insets_5, 0, 0));
		} catch (IOException e) {
		}
		var label_1 = new JLabel("Network:");
		var network_combobox = new JComboBox<>();
		network_combobox.setModel(new EnumComboBoxModel<>(CrptoNetworks.class));
		var label_2 = new JLabel("Text type:");
		var combobox_1 = new JComboBox<>(new String[] { "Phrase", "HEX", "Base64" });
		panel.add(label_1, new GridBagConstraints(1, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(network_combobox, new GridBagConstraints(2, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(label_2, new GridBagConstraints(3, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(combobox_1, new GridBagConstraints(4, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		var text_area = new JTextArea(5, 30);
		var scr_pane = new JScrollPane(text_area);
		panel.add(scr_pane, new GridBagConstraints(1, 1, 4, 3, 0, 0, 17, 0, new Insets(5, 5, 0, 5), 0, 0));
		var btn_1 = new JButton("OK");
		btn_1.addActionListener(e -> Util.submit(() -> {
			CrptoNetworks nw = CrptoNetworks.valueOf(network_combobox.getSelectedItem().toString());
			String type = combobox_1.getSelectedItem().toString();
			String text = text_area.getText().trim();

			boolean b = false;
			byte[] public_key, private_key;
			try {
				private_key = CryptoUtil.getPrivateKey(nw, type, text);
				public_key = CryptoUtil.getPublicKey(nw, private_key);
				b = MyDb.insertAccount(nw, public_key, private_key);
			} catch (Exception x) {
				JOptionPane.showMessageDialog(dialog, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (b) {
				dialog.dispose();
				reload_accounts();
			} else {
				JOptionPane.showMessageDialog(dialog, "Something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}));
		var panel_1 = new JPanel(new BorderLayout());
		panel_1.add(panel, BorderLayout.CENTER);
		var panel_2 = new JPanel(new FlowLayout());
		panel_2.add(btn_1);
		panel_1.add(panel_2, BorderLayout.SOUTH);

		dialog.add(panel_1);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(w);
		dialog.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	private static final void create_watch_account_dialog(Component c) {
		var w = SwingUtilities.getWindowAncestor(c);
		var dialog = new JDialog(w, "Watch Account", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		IndepandentWindows.add(dialog);
		var panel = new JPanel(new GridBagLayout());
		try {
			panel.add(new JLabel(new MyStretchIcon(ImageIO.read(MyToolbar.class.getClassLoader().getResource("icon/" + "eyeglasses.svg")), 64, 64)),
					new GridBagConstraints(0, 0, 1, 4, 0, 0, 17, 0, insets_5, 0, 0));
		} catch (IOException e) {
		}
		var label_1 = new JLabel("Network:");
		var network_combobox = new JComboBox<>();
		network_combobox.setModel(new EnumComboBoxModel<>(CrptoNetworks.class));
		panel.add(label_1, new GridBagConstraints(1, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(network_combobox, new GridBagConstraints(2, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		var text_field = new JTextField(30);
		panel.add(text_field, new GridBagConstraints(1, 1, 4, 3, 0, 0, 10, 1, new Insets(5, 5, 0, 5), 0, 0));
		var btn_1 = new JButton("OK");
		btn_1.addActionListener(e -> Util.submit(() -> {
			CrptoNetworks nw = CrptoNetworks.valueOf(network_combobox.getSelectedItem().toString());
			String text = text_field.getText().trim();

			boolean b = false;
			byte[] public_key, private_key = new byte[] {};
			try {
				public_key = CryptoUtil.getPublicKeyFromAddress(nw, text);
				if (public_key == null) {
					throw new Exception("Reed-Solomon address does not contain public key");
				}
				b = MyDb.insertAccount(nw, public_key, private_key);
			} catch (Exception x) {
				JOptionPane.showMessageDialog(dialog, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (b) {
				dialog.dispose();
				reload_accounts();
			} else {
				JOptionPane.showMessageDialog(dialog, "Something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}));
		var panel_1 = new JPanel(new BorderLayout());
		panel_1.add(panel, BorderLayout.CENTER);
		var panel_2 = new JPanel(new FlowLayout());
		panel_2.add(btn_1);
		panel_1.add(panel_2, BorderLayout.SOUTH);

		dialog.add(panel_1);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(w);
		dialog.setVisible(true);
	}

}
