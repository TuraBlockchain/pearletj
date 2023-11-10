package hk.zdl.crypto.pearlet.component;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.lock.CryptoAccount;
import hk.zdl.crypto.pearlet.ui.SpinableIcon;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

@SuppressWarnings("serial")
public class SetAccountInfoPanel extends JPanel {

	private static final Dimension FIELD_DIMENSION = new Dimension(500, 20);
	private final ResourceBundle rsc_bdl = Util.getResourceBundle();
	private final JComboBox<String> acc_combo_box = new JComboBox<>();
	private final JSlider fee_slider = new JSlider();
	private int decimalPlaces = 8;

	private CryptoNetwork network;
	private String account;

	public SetAccountInfoPanel() {
		super(new GridBagLayout());
		EventBus.getDefault().register(this);
		var label_1 = new JLabel(rsc_bdl.getString("GENERAL_ACCOUNT"));
		add(label_1, newGridConst(0, 0, 3, 17));
		acc_combo_box.setPreferredSize(new Dimension(300, 20));
		add(acc_combo_box, newGridConst(0, 1, 3));

		acc_combo_box.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));

		var label_4 = new JLabel("Name");
		add(label_4, newGridConst(0, 2, 3, 17));
		var name_field = new JTextField();
		name_field.setPreferredSize(FIELD_DIMENSION);
		add(name_field, newGridConst(0, 3, 5));

		var label_5 = new JLabel(rsc_bdl.getString("GENERAL_DESC"));
		add(label_5, newGridConst(0, 4, 3, 17));
		var desc_field = new JTextField();
		desc_field.setPreferredSize(FIELD_DIMENSION);
		add(desc_field, newGridConst(0, 5, 5));

		var label_6 = new JLabel(rsc_bdl.getString("GENERAL_FEE"));
		add(label_6, newGridConst(0, 6, 3, 17));
		var fee_field = new JTextField("0.05");
		var fee_panel = new JPanel(new GridLayout(1, 0));
		fee_panel.setPreferredSize(FIELD_DIMENSION);
		Stream.of(fee_field, fee_slider).forEach(fee_panel::add);
		fee_field.setEditable(false);
		fee_slider.addChangeListener(e -> fee_field.setText(new BigDecimal(fee_slider.getValue()).movePointLeft(decimalPlaces).toPlainString()));
		add(fee_panel, newGridConst(0, 7, 5));

		var send_icon = UIUtil.getStretchIcon("toolbar/paper-plane-solid.svg", 32, 32);
		var send_btn = new JButton(rsc_bdl.getString("SET_ACCOUNT_INFO_PANEL_BTN_TEXT"), send_icon);
		try {
			var btn_img = ImageIO.read(Util.getResource("icon/spinner-solid.svg"));
			var busy_icon = new SpinableIcon(btn_img, 32, 32);
			send_btn.setDisabledIcon(busy_icon);
			busy_icon.start();
		} catch (IOException x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
		}
		add(send_btn, new GridBagConstraints(4, 0, 1, 3, 0, 0, 10, 1, new Insets(5, 5, 5, 0), 0, 0));
		send_btn.addActionListener(e -> {
			if (name_field.getText().isBlank()) {
				JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("SET_ACCOUNT_INFO_PANEL_NAME_CANNOT_BE_EMPTY"), null, JOptionPane.ERROR_MESSAGE);
				return;
			} else if (desc_field.getText().isBlank()) {
				JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("SET_ACCOUNT_INFO_PANEL_DESC_CANNOT_BE_EMPTY"), null, JOptionPane.ERROR_MESSAGE);
				return;
			}
			send_btn.setEnabled(false);
			Util.submit(() -> {
				if (network.isBurst()) {
					var o_r = CryptoAccount.getAccount(network, account);
					if (o_r.isPresent()) {
						try {
							var feeNQT = fee_slider.getValue();
							var public_key = o_r.get().getPublicKey();
							var private_key = o_r.get().getPrivateKey();
							var ugsigned_tx = CryptoUtil.setAccountInfo(network, name_field.getText().trim(), desc_field.getText().trim(), feeNQT, public_key);
							var signed_tx = CryptoUtil.signTransaction(network, private_key, ugsigned_tx);
							CryptoUtil.broadcastTransaction(network, signed_tx);
							UIUtil.displayMessage(rsc_bdl.getString("SET_ACCOUNT_INFO_MESSAGE_TITLE"), rsc_bdl.getString("SET_ACCOUNT_INFO_MESSAGE_TEXT"), MessageType.INFO);
						} catch (Exception x) {
							UIUtil.displayMessage(x.getClass().getSimpleName(), x.getMessage(), MessageType.ERROR);
						}
					}
				}
				send_btn.setEnabled(true);
			});
		});
	}

	private static final GridBagConstraints newGridConst(int x, int y, int width) {
		var a = new GridBagConstraints();
		a.gridx = x;
		a.gridy = y;
		a.gridwidth = width;
		return a;
	}

	private static final GridBagConstraints newGridConst(int x, int y, int width, int anchor) {
		var a = new GridBagConstraints();
		a.gridx = x;
		a.gridy = y;
		a.gridwidth = width;
		a.anchor = anchor;
		return a;
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		acc_combo_box.setModel(new DefaultComboBoxModel<String>(new String[] { e.account }));
		this.network = e.network;
		this.account = e.account;
		Util.submit(() -> {
			decimalPlaces = CryptoUtil.getConstants(network).getInt("decimalPlaces");
			var g = CryptoUtil.getFeeSuggestion(network);
			fee_slider.setMinimum(g.getCheapFee().toNQT().intValue());
			fee_slider.setMaximum(g.getPriorityFee().toNQT().intValue());
			fee_slider.setValue(g.getStandardFee().toNQT().intValue());
			return null;
		});
	}
}
