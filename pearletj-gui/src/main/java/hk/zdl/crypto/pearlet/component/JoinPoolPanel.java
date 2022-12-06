package hk.zdl.crypto.pearlet.component;

import static hk.zdl.crypto.pearlet.util.CrptoNetworks.ROTURA;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class JoinPoolPanel extends JPanel implements ActionListener {

	private static final String NONE = "None";
	private static final long serialVersionUID = -8477305312112985648L;
	private final JProgressBar bar = new JProgressBar();
	private CrptoNetworks network;
	private String account;

	public JoinPoolPanel() {
		super(new GridBagLayout());
		EventBus.getDefault().register(this);
		add(new JLabel("Pool Address:"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(bar, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		var btn = new JButton("Change");
		add(btn, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		btn.addActionListener(this);
		bar.setStringPainted(true);
		bar.setString(NONE);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		this.network = e.network;
		this.account = e.account;
		if (Arrays.asList(CrptoNetworks.ROTURA, CrptoNetworks.SIGNUM).contains(network)) {
			try {
				bar.setString(CryptoUtil.getRewardRecipient(network, account).orElse(NONE));
			} catch (Exception x) {
				JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		var str = JOptionPane.showInputDialog(getRootPane(), "Please specify pool address:", "Pool Address", JOptionPane.QUESTION_MESSAGE);
		if (str != null) {
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
					} else {
						JOptionPane.showMessageDialog(getRootPane(), "Account not found in database!", "ERROR", JOptionPane.ERROR_MESSAGE);
						return;
					}
					var fee_qnt = CryptoUtil.getFeeSuggestion(network).getCheapFee().toNQT();
					var fee_dml = new BigDecimal(fee_qnt, network == ROTURA ? CryptoUtil.peth_decimals : 8);
					byte[] unsigned = CryptoUtil.setRewardRecipient(network, account, public_key, str, fee_dml);
					byte[] signed = CryptoUtil.signTransaction(network, private_key, unsigned);
					CryptoUtil.broadcastTransaction(network, signed);
					UIUtil.displayMessage("Join Pool", "Join pool complete.", null);
				} catch (Exception x) {
					JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
				} finally {
					bar.setIndeterminate(false);
					((JButton) e.getSource()).setEnabled(true);
				}
			});
		}
	}

}
