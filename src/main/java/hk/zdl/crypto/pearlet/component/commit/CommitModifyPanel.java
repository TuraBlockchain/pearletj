package hk.zdl.crypto.pearlet.component.commit;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.greenrobot.eventbus.EventBus;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.component.event.BalanceUpdateEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.lock.CryptoAccount;
import hk.zdl.crypto.pearlet.ui.SpinableIcon;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.ui.WaitLayerUI;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class CommitModifyPanel extends JPanel implements ActionListener {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private static final long serialVersionUID = -4996189634836777005L;
	private final JLayer<JPanel> jlayer = new JLayer<>();
	private final WaitLayerUI wuli = new WaitLayerUI();
	private final ChartPanel chart_panel = new ChartPanel(ChartFactory.createPieChart(null, new DefaultPieDataset<String>(), true, true, false));
	private final JButton btn = new JButton("Commit", UIUtil.getStretchIcon("toolbar/paper-plane-solid.svg", 32, 32));
	private final SpinableIcon busy_icon = new SpinableIcon(new BufferedImage(32, 32, BufferedImage.TYPE_4BYTE_ABGR), 32, 32);

	private BigDecimal committed_balance = null;
	private CryptoNetwork network;
	private String account;

	public CommitModifyPanel() {
		super(new GridBagLayout());
		var top_panel = new JPanel(new FlowLayout());
		btn.setFont(new Font("Arial Black", Font.PLAIN, 32));
		btn.setMultiClickThreshhold(300);
		btn.setEnabled(false);
		try {
			var btn_img = ImageIO.read(Util.getResource("icon/spinner-solid.svg"));
			busy_icon.setImage(btn_img, 32, 32);
			btn.setDisabledIcon(busy_icon);
		} catch (IOException x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
		}
		top_panel.add(btn);
		jlayer.setView(chart_panel);
		jlayer.setUI(wuli);
		add(top_panel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(jlayer, new GridBagConstraints(0, 1, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		btn.addActionListener(this);
		busy_icon.start();
		SwingUtilities.invokeLater(() -> init_chart());
	}

	private void init_chart() {
		var p = chart_panel;
		p.setMouseWheelEnabled(false);
		p.setMouseZoomable(false);
		p.setRangeZoomable(false);
		p.setPopupMenu(null);
		var chart = p.getChart();
		var plot = chart.getPlot();
		var trans = new Color(0, 0, 0, 0);
		chart.getLegend().setBackgroundPaint(null);
		chart.setBackgroundPaint(trans);
		plot.setBackgroundPaint(trans);
		plot.setOutlinePaint(null);
	}

	public void onMessage(AccountChangeEvent e) {
		this.network = e.network;
		this.account = e.account;
		if (network == null || account == null)
			return;
		btn.setEnabled(false);
		busy_icon.start();
		wuli.start();
		BigDecimal _bal = new BigDecimal(0), _c_bal = new BigDecimal(0), _a_bal = new BigDecimal(0);
		try {
			var account = CryptoUtil.getAccount(e.network, e.account);
			var balance = account.getBalance();
			var committed_balance = account.getCommittedBalance();
			var decimalPlaces = CryptoUtil.getConstants(network).getInt("decimalPlaces");
			_bal = new BigDecimal(balance.toNQT(), decimalPlaces);
			_c_bal = new BigDecimal(committed_balance.toNQT(), decimalPlaces);
			_a_bal = _bal.subtract(_c_bal);
			EventBus.getDefault().post(new BalanceUpdateEvent(e.network, e.account, _a_bal));
		} catch (Exception x) {
		}
		committed_balance = _c_bal;

		var chart = chart_panel.getChart();
		chart.setTitle(e.account);
		@SuppressWarnings("unchecked")
		var plot = (PiePlot<String>) chart.getPlot();
		var dataset = new DefaultPieDataset<String>();
		dataset.setValue("Available Balance", _a_bal);
		dataset.setValue("Committed Balance", _c_bal);
		plot.setDataset(dataset);
		plot.setSectionPaint("Available Balance", Color.green.darker());
		plot.setSectionPaint("Committed Balance", Color.blue.darker());
		btn.setEnabled(true);
		busy_icon.stop();
		wuli.stop();
		SwingUtilities.updateComponentTreeUI(chart_panel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		var panel = new JPanel(new GridBagLayout());
		panel.add(new JLabel("Action"), new GridBagConstraints(0, 0, 1, 1, 0, 0, 17, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var add_btn = new JRadioButton("Add", true);
		var rvk_btn = new JRadioButton("Revoke", false);
		var g = new ButtonGroup();
		g.add(add_btn);
		g.add(rvk_btn);
		panel.add(add_btn, new GridBagConstraints(1, 0, 1, 1, 0, 0, 17, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		panel.add(rvk_btn, new GridBagConstraints(2, 0, 1, 1, 0, 0, 17, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		panel.add(new JLabel("Amount"), new GridBagConstraints(0, 1, 1, 1, 0, 0, 17, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var txt_field = new JTextField();
		panel.add(txt_field, new GridBagConstraints(1, 1, 2, 1, 0, 0, 17, GridBagConstraints.HORIZONTAL, insets_5, 0, 0));
		var a = new JLabel();
		a.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(new JLabel("Tx fee:"), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(a, new GridBagConstraints(1, 2, 2, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		Util.submit(() -> {
			var fee = CryptoUtil.getFeeSuggestion(network).getCheapFee().toNQT();
			a.setText(new BigDecimal(fee).movePointLeft(CryptoUtil.getConstants(network).getInt("decimalPlaces")).toPlainString());
			return null;
		});
		Util.submit(() -> {
			var pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
			var dlg = pane.createDialog(getRootPane(), "Set Commitment");
			dlg.addWindowFocusListener(new WindowAdapter() {
				@Override
				public void windowGainedFocus(WindowEvent e) {
					txt_field.grabFocus();
				}
			});
			dlg.setVisible(true);

			if ((int) pane.getValue() == JOptionPane.OK_OPTION) {
				var amount = new BigDecimal(0);
				try {
					amount = new BigDecimal(txt_field.getText().trim());
				} catch (Exception x) {
					JOptionPane.showMessageDialog(getRootPane(), "Invalid Number!", "ERROR", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				if (amount.compareTo(new BigDecimal(0)) == 0) {
					return null;
				} else if (amount.doubleValue() < 0) {
					JOptionPane.showMessageDialog(getRootPane(), "Negetive Number Not Allowed!", "ERROR", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				var opt_r = CryptoAccount.getAccount(network, account);
				if (opt_r.isPresent()) {
				} else {
					JOptionPane.showMessageDialog(getRootPane(), "Account not found in database!", "ERROR", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				try {
					var public_key = opt_r.get().getPublicKey();
					var private_key = opt_r.get().getPrivateKey();
					var fee_qnt = CryptoUtil.getFeeSuggestion(network).getCheapFee().toNQT();
					var fee_dml = new BigDecimal(fee_qnt, CryptoUtil.getConstants(network).getInt("decimalPlaces"));
					btn.setEnabled(false);
					busy_icon.start();
					wuli.start();
					if (add_btn.isSelected()) {
						do_add_commit(public_key, private_key, amount, fee_dml);
					} else {
						do_revoke_commit(public_key, private_key, amount, fee_dml);
					}
				} catch (Exception x) {
					SwingUtilities.invokeLater(() -> {
						btn.setEnabled(true);
						busy_icon.stop();
						wuli.stop();
						JOptionPane.showMessageDialog(getRootPane(), x.getMessage(), x.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
						if (x.getMessage().trim().contains("Not enough funds")) {
							Util.submit(new RefreshChart(committed_balance, account));
						}
					});
					return null;
				}
				UIUtil.displayMessage("Commitment", "Commitment is set.");
				Util.submit(new RefreshChart(committed_balance, account));
			}
			return null;
		});
	}

	private void do_add_commit(byte[] public_key, byte[] private_key, BigDecimal amount, BigDecimal fee) throws Exception {
		byte[] unsigned = CryptoUtil.addCommitment(network, public_key, amount, fee);
		byte[] signed = CryptoUtil.signTransaction(network, private_key, unsigned);
		CryptoUtil.broadcastTransaction(network, signed);
	}

	private void do_revoke_commit(byte[] public_key, byte[] private_key, BigDecimal amount, BigDecimal fee) throws Exception {
		byte[] unsigned = CryptoUtil.removeCommitment(network, public_key, amount, fee);
		byte[] signed = CryptoUtil.signTransaction(network, private_key, unsigned);
		CryptoUtil.broadcastTransaction(network, signed);
	}

	private class RefreshChart implements Callable<Void> {

		private final BigDecimal old_committed_balance;
		private final String acc;

		RefreshChart(BigDecimal old_committed_balance, String acc) {
			super();
			this.old_committed_balance = old_committed_balance;
			this.acc = acc;
		}

		@Override
		public Void call() throws Exception {
			for (var j = 0; j < 10; j++) {
				btn.setEnabled(false);
				busy_icon.start();
				wuli.start();
				TimeUnit.SECONDS.sleep(2);
				onMessage(new AccountChangeEvent(network, account));
				if (acc != account || committed_balance.intValue() != old_committed_balance.intValue()) {
					break;
				}
			}
			return null;
		}

	}

}
