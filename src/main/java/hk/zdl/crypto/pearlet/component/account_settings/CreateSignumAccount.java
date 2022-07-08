package hk.zdl.crypto.pearlet.component.account_settings;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
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
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.util.Base64;

import hk.zdl.crypto.pearlet.component.MyStretchIcon;
import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.misc.IndepandentWindows;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class CreateSignumAccount {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	public static final void create_new_account_dialog(Component c, CrptoNetworks nw) {
		var w = SwingUtilities.getWindowAncestor(c);
		var dialog = new JDialog(w, "Create New Account", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		IndepandentWindows.add(dialog);
		var panel = new JPanel(new GridBagLayout());
		try {
			panel.add(new JLabel(new MyStretchIcon(ImageIO.read(Util.getResource("icon/" + "cloud-plus-fill.svg")), 64, 64)), new GridBagConstraints(0, 0, 1, 4, 0, 0, 17, 0, insets_5, 0, 0));
		} catch (IOException e) {
		}
		var label_1 = new JLabel("Network:");
		var network_combobox = new JComboBox<>(new Object[] { nw });
		network_combobox.setEnabled(false);
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
				Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
			} else {
				JOptionPane.showMessageDialog(dialog, "Something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}));
		combobox_1.addActionListener((e) -> btn_1.doClick());

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
}
