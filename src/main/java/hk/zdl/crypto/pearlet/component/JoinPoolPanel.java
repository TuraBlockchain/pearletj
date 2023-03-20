package hk.zdl.crypto.pearlet.component;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class JoinPoolPanel extends JPanel implements ActionListener {

	private static final String NONE = "None";
	private static final long serialVersionUID = -8477305312112985648L;
	private final JProgressBar bar = new JProgressBar();
	private final JButton btn = new JButton("Change");
	private CryptoNetwork network;
	private String account;

	public JoinPoolPanel() {
		super(new GridBagLayout());
		EventBus.getDefault().register(this);
		add(new JLabel("Reward Recipient:"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(bar, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		add(btn, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		btn.addActionListener(this);
		bar.setStringPainted(true);
		bar.setString(NONE);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		this.network = e.network;
		this.account = e.account;
		bar.setString("");
		bar.setIndeterminate(true);
		btn.setEnabled(false);
		if (account == null) {
			return;
		}
		if (network.isBurst()) {
			update();
			btn.setEnabled(true);
		} else {
			btn.setEnabled(false);
		}
		bar.setIndeterminate(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		var panel = new JPanel(new GridBagLayout());
		var txt_field = new JTextField(30);
		var label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(new JLabel("Please specify reward recipient:"), new GridBagConstraints(0, 0, 2, 1, 2, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(txt_field, new GridBagConstraints(0, 1, 2, 1, 2, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(new JLabel("Tx fee:"), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(label, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		Util.submit(() -> {
			var fee = CryptoUtil.getFeeSuggestion(network).getCheapFee();
			var decimalPlaces = CryptoUtil.getConstants(network).getInt("decimalPlaces");
			var txt = new BigDecimal(fee.toNQT(), decimalPlaces).toPlainString();
			label.setText(txt);
			return null;
		});
		int i = JOptionPane.showConfirmDialog(getRootPane(), panel, "Reward Recipient", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (i == JOptionPane.OK_OPTION) {
			var target = txt_field.getText();
			if (target.isBlank()) {
				JOptionPane.showMessageDialog(getRootPane(), "Reward recipient cannot be empty!", "ERROR", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Util.submit(() -> {
				try {
					bar.setIndeterminate(true);
					((JButton) e.getSource()).setEnabled(false);
					var public_key = new byte[] {};
					var private_key = new byte[] {};
					Optional<Record> opt_r = MyDb.getAccount(network, account);
					if (opt_r.isPresent()) {
						public_key = opt_r.get().getBytes("PUBLIC_KEY");
						private_key = opt_r.get().getBytes("PRIVATE_KEY");
						if (private_key.length < 1) {
							JOptionPane.showMessageDialog(getRootPane(), "Cannot modify watch account!", "ERROR", JOptionPane.ERROR_MESSAGE);
							return;
						}
					} else {
						JOptionPane.showMessageDialog(getRootPane(), "Account not found in database!", "ERROR", JOptionPane.ERROR_MESSAGE);
						return;
					}
					var fee_qnt = CryptoUtil.getFeeSuggestion(network).getCheapFee().toNQT();
					var decimalPlaces = CryptoUtil.getConstants(network).getInt("decimalPlaces");
					var fee_dml = new BigDecimal(fee_qnt, decimalPlaces);
					check_permission();
					byte[] unsigned = CryptoUtil.setRewardRecipient(network, account, public_key, target, fee_dml);
					byte[] signed = CryptoUtil.signTransaction(network, private_key, unsigned);
					CryptoUtil.broadcastTransaction(network, signed);
					UIUtil.displayMessage("Join Pool", "Join pool complete.");
					update();
				} catch (Exception x) {
					UIUtil.displayMessage(x.getClass().getSimpleName(), x.getMessage(), MessageType.ERROR);
				} finally {
					bar.setIndeterminate(false);
					((JButton) e.getSource()).setEnabled(true);
				}
			});
		}
	}

	private void check_permission() throws Exception {
		if (network.isBurst() && network.getUrl().toLowerCase().contains("mainnet.peth.world")) {
			var committed_balance = CryptoUtil.getAccount(network, this.account).getCommittedBalance();
			var decimalPlaces = CryptoUtil.getConstants(network).getInt("decimalPlaces");
			var _c_bal = new BigDecimal(committed_balance.toNQT(), decimalPlaces);
			if (_c_bal.compareTo(new BigDecimal("10000")) < 0) {
				throw new IllegalStateException("Insufficient Commitment!");
			}
		}
	}
	
	private void update() {
		var n = network;
		var a = account;
		for (int j = 0; j < 100; j++) {
			try {
				bar.setString(CryptoUtil.getRewardRecipient(network, account).orElse(NONE));
				break;
			} catch (Exception x) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
			}
			if (n != network || a != account) {
				break;
			}
		}
	}

}
