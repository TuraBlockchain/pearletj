package hk.zdl.crypto.pearlet.component;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.util.ResourceBundle;
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

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.lock.CryptoAccount;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.SignumAddress;

public class JoinPoolPanel extends JPanel implements ActionListener {

	private static ResourceBundle rsc_bdl = Util.getResourceBundle();
	private static final long serialVersionUID = -8477305312112985648L;
	private final JProgressBar bar = new JProgressBar();
	private final JButton btn = new JButton(rsc_bdl.getString("JOIN_POOL_BTN_TEXT"));
	private CryptoNetwork network;
	private BigInteger cheap_fee = BigInteger.ZERO;
	private String account;

	public JoinPoolPanel() {
		super(new GridBagLayout());
		EventBus.getDefault().register(this);
		add(new JLabel(rsc_bdl.getString("JOIN_POOL_RCP_LABEL")), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(bar, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		add(btn, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		btn.addActionListener(this);
		bar.setStringPainted(true);
		bar.setString(rsc_bdl.getString("GENERAL_NONE"));
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
		panel.add(new JLabel(rsc_bdl.getString("JOIN_POOL_LABEL_1")), new GridBagConstraints(0, 0, 2, 1, 2, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(txt_field, new GridBagConstraints(0, 1, 2, 1, 2, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(new JLabel(rsc_bdl.getString("GENERAL_FEE")), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(label, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		Util.submit(() -> {
			var decimalPlaces = CryptoUtil.getConstants(network).getInt("decimalPlaces");
			var txt = new BigDecimal(cheap_fee, decimalPlaces).toPlainString();
			label.setText(txt);
			return null;
		});
		var pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		var dlg = pane.createDialog(getRootPane(), rsc_bdl.getString("JOIN_POOL_DIALOG_TITLE"));
		dlg.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				txt_field.grabFocus();
			}
		});
		dlg.setVisible(true);
		
		if (pane.getValue().equals(JOptionPane.OK_OPTION)) {
			var target = txt_field.getText();
			if (target.isBlank()) {
				JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("JOIN_POOL_ERR_1"), rsc_bdl.getString("GENERAL_ERROR"), JOptionPane.ERROR_MESSAGE);
				return;
			} else if (SignumAddress.fromEither(target) == null) {
				JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("JOIN_POOL_ERR_2"), rsc_bdl.getString("GENERAL_ERROR"), JOptionPane.ERROR_MESSAGE);
				return;
			}
			Util.submit(() -> {
				try {
					bar.setString("");
					bar.setIndeterminate(true);
					btn.setEnabled(false);

					var public_key = new byte[] {};
					var private_key = new byte[] {};
					var opt_r = CryptoAccount.getAccount(network, account);
					if (opt_r.isPresent()) {
						public_key = opt_r.get().getPublicKey();
						private_key = opt_r.get().getPrivateKey();
						if (private_key.length < 1) {
							JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("JOIN_POOL_ERR_3"), rsc_bdl.getString("GENERAL_ERROR"), JOptionPane.ERROR_MESSAGE);
							return;
						}
					} else {
						JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("JOIN_POOL_ERR_4"), rsc_bdl.getString("GENERAL_ERROR"), JOptionPane.ERROR_MESSAGE);
						return;
					}
					check_permission();
					var fee_dml = new BigDecimal(cheap_fee, CryptoUtil.getConstants(network).getInt("decimalPlaces"));
					byte[] unsigned = CryptoUtil.setRewardRecipient(network, account, public_key, target, fee_dml);
					byte[] signed = CryptoUtil.signTransaction(network, private_key, unsigned);
					CryptoUtil.broadcastTransaction(network, signed);
					UIUtil.displayMessage(rsc_bdl.getString("JOIN_POOL_TITLE"), rsc_bdl.getString("JOIN_POOL_COMPLETE_MSG_TEXT"));
					update();
				} catch (Exception x) {
					UIUtil.displayMessage(x.getClass().getSimpleName(), x.getMessage(), MessageType.ERROR);
				} finally {
					bar.setIndeterminate(false);
					btn.setEnabled(true);
				}
			});
		}
	}

	private void check_permission() throws Exception {
		if (network.isBurst() && network.getUrl().toLowerCase().contains("mainnet.tura.world")) {
			var committed_balance = CryptoUtil.getAccount(network, this.account).getCommittedBalance();
			var decimalPlaces = CryptoUtil.getConstants(network).getInt("decimalPlaces");
			var _c_bal = new BigDecimal(committed_balance.toNQT(), decimalPlaces);
			if (_c_bal.compareTo(new BigDecimal("10000")) < 0) {
				throw new IllegalStateException(rsc_bdl.getString("JOIN_POOL_ERR_5"));
			}
		}
	}

	private void update() {
		var n = network;
		var a = account;
		Util.submit(() -> cheap_fee = CryptoUtil.getFeeSuggestion(network).getCheapFee().toNQT());
		for (int j = 0; j < 100; j++) {
			try {
				bar.setString(CryptoUtil.getRewardRecipient(network, account).orElse(rsc_bdl.getString("GENERAL_NONE")));
				break;
			} catch (RuntimeException | SocketTimeoutException | InterruptedException | ThreadDeath x) {
			} catch (Exception x) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
			}
			if (n != network || a != account) {
				break;
			}
		}
	}

}
