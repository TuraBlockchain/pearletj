package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;

import hk.zdl.crypto.pearlet.ui.RoundJTextField;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class AliseSearchPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4782115342409404512L;
	private static final int icon_size = 16;
	private static EnsResolver r = null;
	private final CardLayout card_layout_1 = new CardLayout(5, 5);
	private final JPanel center_card_panel = new JPanel(card_layout_1);
	private final RoundJTextField field_1 = new RoundJTextField(30);
	private final RoundJTextField field_2 = new RoundJTextField(30);
	private final ResourceBundle rsc_bdl = Util.getResourceBundle();

	public AliseSearchPanel() {
		super(new BorderLayout());
		var center_panel = new JPanel(new GridBagLayout());
		var label_1 = new JLabel(rsc_bdl.getString("ALISE_SEARCH_PANEL_NAME"));
		var label_2 = new JLabel(rsc_bdl.getString("ALISE_SEARCH_PANEL_ADDRESS"));
		center_panel.add(label_1, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
		center_panel.add(label_2, new GridBagConstraints(0, 2, 1, 1, 0, 0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
		center_panel.add(field_1, new GridBagConstraints(1, 0, 1, 1, 0, 0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
		center_panel.add(field_2, new GridBagConstraints(1, 2, 1, 1, 0, 0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
		var resolve_buttons_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		center_card_panel.add(resolve_buttons_panel, "buttons");
		var progress_bar = new JProgressBar();
		progress_bar.setIndeterminate(true);
		center_card_panel.add(progress_bar, "progress_bar");
		center_panel.add(center_card_panel, new GridBagConstraints(1, 1, 1, 1, 0, 0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
		var resolve_btn = new JButton(rsc_bdl.getString("ALISE_SEARCH_PANEL_RESOLVE"), UIUtil.getStretchIcon("icon/" + "arrow-down.svg", icon_size, icon_size));
		var r_resolve_btn = new JButton(rsc_bdl.getString("ALISE_SEARCH_PANEL_REVERSE_RESOLVE"), UIUtil.getStretchIcon("icon/" + "arrow-up.svg", icon_size, icon_size));
		resolve_buttons_panel.add(resolve_btn);
		resolve_buttons_panel.add(r_resolve_btn);
		Stream.of(label_1, label_2, field_1, field_2, resolve_btn, r_resolve_btn).forEach(o -> o.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20)));
		add(center_panel, BorderLayout.CENTER);

		resolve_btn.addActionListener((e) -> start_resolve());
		r_resolve_btn.addActionListener((e) -> r_resolve_btn());

	}

	public void setIndeterminate(boolean b) {
		if (b) {
			card_layout_1.show(center_card_panel, "progress_bar");
		} else {
			card_layout_1.show(center_card_panel, "buttons");
		}
		Stream.of(field_1, field_2).forEach(o -> o.setEnabled(!b));
	}

	protected void start_resolve() {
		if (field_1.getText().isBlank()) {
			JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("ALISE_SEARCH_PANEL_NAME_CANNOT_BE_EMPTY"), rsc_bdl.getString("GENERAL_ERROR"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		field_2.setText(null);
		setIndeterminate(true);
		Util.submit(() -> {
			buildResolverIfAbsent();
			if (r == null) {
				setIndeterminate(false);
				JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("ALISE_SEARCH_PANEL_CANNOT_LOAD_ENS"), rsc_bdl.getString("GENERAL_ERROR"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			Future<String> future = Util.submit(() -> r.resolve(field_1.getText().trim()));
			String result = null;
			try {
				result = future.get(30, TimeUnit.SECONDS);
			} catch (Exception e) {
			} finally {
				field_2.setText(result);
				setIndeterminate(false);
			}
		});
	}

	protected void r_resolve_btn() {
		if (field_2.getText().isBlank()) {
			JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("ALISE_SEARCH_PANEL_ADDR_CANNOT_BE_EMPTY"), rsc_bdl.getString("GENERAL_ERROR"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		field_1.setText(null);
		setIndeterminate(true);
		Util.submit(() -> {
			buildResolverIfAbsent();
			if (r == null) {
				setIndeterminate(false);
				JOptionPane.showMessageDialog(getRootPane(), rsc_bdl.getString("ALISE_SEARCH_PANEL_CANNOT_LOAD_ENS"), rsc_bdl.getString("GENERAL_ERROR"), JOptionPane.ERROR_MESSAGE);
				return;
			}
			Future<String> future = Util.submit(() -> r.reverseResolve(field_2.getText().trim()));
			String result = null;
			try {
				result = future.get(30, TimeUnit.SECONDS);
			} catch (Exception e) {
			} finally {
				field_1.setText(result);
				setIndeterminate(false);
			}
		});
	}

	private static final synchronized void buildResolverIfAbsent() {
		if (r == null) {
			Optional<Web3j> o_j = CryptoUtil.getWeb3j();
			if (o_j.isPresent()) {
				r = new EnsResolver(o_j.get());
			}
		}
	}
}
