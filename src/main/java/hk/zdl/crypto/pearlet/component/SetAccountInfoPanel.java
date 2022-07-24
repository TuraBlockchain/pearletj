package hk.zdl.crypto.pearlet.component;

import static hk.zdl.crypto.pearlet.util.CrptoNetworks.ROTURA;
import static hk.zdl.crypto.pearlet.util.CrptoNetworks.SIGNUM;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
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

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.MyToolbar;
import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.SpinableIcon;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.SignumValue;

@SuppressWarnings("serial")
public class SetAccountInfoPanel extends JPanel {

	private static final Dimension FIELD_DIMENSION = new Dimension(500, 20);
	private final JComboBox<String> acc_combo_box = new JComboBox<>();

	private CrptoNetworks network;
	private String account;

	public SetAccountInfoPanel() {
		super(new GridBagLayout());
		EventBus.getDefault().register(this);
		var label_1 = new JLabel("Account");
		add(label_1, newGridConst(0, 0, 3, 17));
		acc_combo_box.setPreferredSize(new Dimension(300, 20));
		add(acc_combo_box, newGridConst(0, 1, 3));

		acc_combo_box.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));

		var label_4 = new JLabel("Name");
		add(label_4, newGridConst(0, 2, 3, 17));
		var name_field = new JTextField();
		name_field.setPreferredSize(FIELD_DIMENSION);
		add(name_field, newGridConst(0, 3, 5));

		var label_5 = new JLabel("Description");
		add(label_5, newGridConst(0, 4, 3, 17));
		var desc_field = new JTextField();
		desc_field.setPreferredSize(FIELD_DIMENSION);
		add(desc_field, newGridConst(0, 5, 5));

		var label_6 = new JLabel("Fee");
		add(label_6, newGridConst(0, 6, 3, 17));
		var fee_field = new JTextField("0.05");
		fee_field.setHorizontalAlignment(JTextField.RIGHT);
		var fee_panel = new JPanel(new GridLayout(1, 0));
		fee_panel.setPreferredSize(FIELD_DIMENSION);
		var fee_slider = new JSlider(10, 100, 50);
		Stream.of(fee_field, fee_slider).forEach(fee_panel::add);
		fee_field.setEditable(false);
		fee_slider.addChangeListener(e -> fee_field.setText("" + fee_slider.getValue() / 1000f));
		add(fee_panel, newGridConst(0, 7, 5));

		var send_icon = MyToolbar.getIcon("paper-plane-solid.svg");
		var send_btn = new JButton("Update Account Info",send_icon);
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
				JOptionPane.showMessageDialog(getRootPane(), "Name cannot be empty!", null, JOptionPane.ERROR_MESSAGE);
				return;
			} else if (desc_field.getText().isBlank()) {
				JOptionPane.showMessageDialog(getRootPane(), "Description cannot be empty!", null, JOptionPane.ERROR_MESSAGE);
				return;
			}
			send_btn.setEnabled(false);
			Util.submit(()->{
				Optional<Record> o_r = MyDb.getAccount(network, account);
				if (o_r.isPresent()) {
					byte[] private_key = o_r.get().getBytes("PRIVATE_KEY");
					byte[] public_key = o_r.get().getBytes("PUBLIC_KEY");
					if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
						try {
							var feeNQT = SignumValue.fromSigna(new BigDecimal(fee_slider.getValue()).divide(new BigDecimal("1000"))).toNQT().longValue();
							byte[] ugsigned_tx = CryptoUtil.setAccountInfo(network, name_field.getText().trim(), desc_field.getText().trim(), feeNQT, public_key);
							byte[] signed_tx = CryptoUtil.signTransaction(network, private_key, ugsigned_tx);
							CryptoUtil.broadcastTransaction(network, signed_tx);
						} catch (Exception x) {
							JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
	}
}
