package hk.zdl.crypto.pearlet.component.account_settings.signum;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.util.Base64;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.misc.IndepandentWindows;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class CreateSignumAccount {

	private static List<String> mnemoic = null;
	private static final Insets insets_5 = new Insets(5, 5, 5, 5);

	@SuppressWarnings("unchecked")
	public static final void create_new_account_dialog(Component c, CrptoNetworks nw) {
		var w = SwingUtilities.getWindowAncestor(c);
		var dialog = new JDialog(w, "Create New Account", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		IndepandentWindows.add(dialog);
		var panel = new JPanel(new GridBagLayout());
		panel.add(new JLabel(UIUtil.getStretchIcon("icon/" + "cloud-plus-fill.svg", 64, 64)), new GridBagConstraints(0, 0, 1, 4, 0, 0, 17, 0, insets_5, 0, 0));
		var label_1 = new JLabel("Network:");
		var network_combobox = new JComboBox<>(new String[] { nw.toString() });
		network_combobox.setEnabled(false);
		var label_2 = new JLabel("Text type:");
		var combobox_1 = new JComboBox<>(new EnumComboBoxModel<>(PKT.class));
		panel.add(label_1, new GridBagConstraints(1, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(network_combobox, new GridBagConstraints(2, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(label_2, new GridBagConstraints(3, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		panel.add(combobox_1, new GridBagConstraints(4, 0, 1, 1, 0, 0, 17, 0, insets_5, 0, 0));
		var text_area = new JTextArea(5, 30);
		var scr_pane = new JScrollPane(text_area, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		panel.add(scr_pane, new GridBagConstraints(1, 1, 4, 3, 0, 0, 17, 1, insets_5, 0, 0));
		text_area.setEditable(false);
		text_area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, text_area.getFont().getSize()));

		var btn_1 = new JButton("Random");
		var btn_2 = new JButton("Copy");
		var btn_3 = new JButton("OK");
		var panel_1 = new JPanel(new GridBagLayout());
		panel_1.add(btn_1, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		panel_1.add(btn_2, new GridBagConstraints(1, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		panel_1.add(btn_3, new GridBagConstraints(2, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));

		panel.add(panel_1, new GridBagConstraints(0, 5, 5, 1, 0, 0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));

		btn_1.addActionListener(e -> {
			var sb = new StringBuilder();
			byte[] bArr = new byte[32];
			new Random().nextBytes(bArr);
			PKT type = (PKT) combobox_1.getSelectedItem();
			switch (type) {
			case Base64:
				text_area.setText(Base64.encodeBytes(bArr));
				break;
			case HEX:
				for (byte b : bArr) {
					sb.append(String.format("%02X", b));
					sb.append(' ');
				}
				text_area.setText(sb.toString().trim());
				break;
			case Phrase:
				text_area.setText(get_mnemoic());
				break;
			}
		});
		btn_2.addActionListener(e -> {
			var s = new StringSelection(text_area.getText().trim());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, s);
		});
		btn_3.addActionListener(e -> Util.submit(() -> {
			PKT type = (PKT) combobox_1.getSelectedItem();
			String text = text_area.getText().trim();

			boolean b = false;
			byte[] public_key, private_key;
			try {
				private_key = CryptoUtil.getPrivateKey(nw, type, text);
				public_key = CryptoUtil.getPublicKey(nw, private_key);
				b = MyDb.insertAccount(nw, CryptoUtil.getAddress(nw, public_key), public_key, private_key);
			} catch (Exception x) {
				JOptionPane.showMessageDialog(dialog, x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (b) {
				dialog.dispose();
				UIUtil.displayMessage("Create Account", "done!", null);
				Util.submit(() -> EventBus.getDefault().post(new AccountListUpdateEvent(MyDb.getAccounts())));
			} else {
				JOptionPane.showMessageDialog(dialog, "Duplicate Entry!", "Error", JOptionPane.ERROR_MESSAGE);
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

	private static synchronized String get_mnemoic() {
		if (mnemoic == null)
			mnemoic = new BufferedReader(new InputStreamReader(CreateSignumAccount.class.getClassLoader().getResourceAsStream("en-mnemonic-word-list.txt"))).lines().filter(s -> !s.isBlank())
					.map(String::trim).toList();
		var sb = new StringBuilder();
		var rand = new Random();
		for (var i = 0; i < 12; i++) {
			sb.append(mnemoic.get(rand.nextInt(mnemoic.size())));
			sb.append(' ');
		}
		return sb.toString().trim();
	}
}
