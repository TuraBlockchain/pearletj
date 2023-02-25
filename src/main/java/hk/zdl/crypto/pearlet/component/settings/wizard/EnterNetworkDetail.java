package hk.zdl.crypto.pearlet.component.settings.wizard;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONTokener;

import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.Util;
import se.gustavkarlsson.gwiz.AbstractWizardPage;

public class EnterNetworkDetail extends AbstractWizardPage {

	private static final long serialVersionUID = 1085301247361719995L;
	private final ButtonGroup btn_grp = new ButtonGroup();

	public EnterNetworkDetail() {
		super(new CardLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Provide Network Detail", TitledBorder.LEFT, TitledBorder.TOP,
				new Font("Arial Black", Font.PLAIN, (int) (getFont().getSize() * 1.5))));
		var burst_panel = new JPanel();
		var eth_panel = new JPanel();
		add(new JScrollPane(burst_panel), "Burst Variant");
		add(new JScrollPane(eth_panel), "Ethereum");
		var in_burst_panel = new JPanel(new GridLayout(0, 1));
		burst_panel.add(in_burst_panel);
		var in_eth_panel = new JPanel(new GridLayout(0, 1));
		eth_panel.add(in_eth_panel);

		Util.submit(() -> {
			add_predefined_networks(in_burst_panel);
			add_signum_networks(in_burst_panel);
			add_custom_networks(in_burst_panel);
			add_web3j_networks(in_eth_panel);
			add_custom_networks(in_eth_panel);
		});
	}

	protected void setNetworkType(String type) {
		((CardLayout) getLayout()).show(this, type);
	}

	private void add_predefined_networks(Container c) {
		var jarr = new JSONArray(new JSONTokener(UIUtil.class.getClassLoader().getResourceAsStream("network/predefined.json")));
		for (var i = 0; i < jarr.length(); i++) {
			var jobj = jarr.getJSONObject(i);
			var name = jobj.getString("networkName");
			var icon = jobj.getString("icon");
			var url = jobj.getString("server url");
			c.add(generate_network_component(name, "icon/" + icon, new String[] { url }));
		}
	}

	private void add_signum_networks(Container c) {
		try {
			var list = IOUtils.readLines(UIUtil.class.getClassLoader().getResourceAsStream("network/signum.txt"), Charset.defaultCharset());
			c.add(generate_network_component("Signum", "icon/Signum_Logomark_black.png", list.toArray(new String[0])));
		} catch (IOException e) {
		}
	}
	private void add_web3j_networks(Container c) {
		try {
			var list = IOUtils.readLines(UIUtil.class.getClassLoader().getResourceAsStream("network/web3j.txt"), Charset.defaultCharset());
			c.add(generate_network_component("Ethereum", "icon/ethereum-crypto-cryptocurrency-2-svgrepo-com.svg", list.toArray(new String[0])));
		} catch (IOException e) {
		}
	}

	private void add_custom_networks(Container c) {
		c.add(generate_network_component("custom", "icon/blockchain-dot-com-svgrepo-com.svg", new String[0]));
	}

	private Component generate_network_component(String name, String icon_path, String[] address) {
		var c = new JPanel(new GridBagLayout());
		c.setPreferredSize(new Dimension(500, 60));
		c.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), name, TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial Black", Font.PLAIN, (getFont().getSize()))));
		var icon = new JLabel(UIUtil.getStretchIcon(icon_path, 32, 32));
		var box = new JComboBox<String>(address);
		if (address.length < 1) {
			box.setEditable(true);
		}
		var ratio = new JRadioButton();
		ratio.setSelected(btn_grp.getButtonCount() == 0);
		btn_grp.add(ratio);
		c.add(icon, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		c.add(box, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		c.add(ratio, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		return c;
	}

	@Override
	protected AbstractWizardPage getNextPage() {
		return null;
	}

	@Override
	protected boolean isCancelAllowed() {
		return true;
	}

	@Override
	protected boolean isPreviousAllowed() {
		return true;
	}

	@Override
	protected boolean isNextAllowed() {
		return true;
	}

	@Override
	protected boolean isFinishAllowed() {
		return false;
	}

}
