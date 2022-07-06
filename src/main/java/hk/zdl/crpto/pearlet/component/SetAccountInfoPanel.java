package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.stream.Stream;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crpto.pearlet.MyToolbar;
import hk.zdl.crpto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crpto.pearlet.ui.WaitLayerUI;
import hk.zdl.crpto.pearlet.util.CrptoNetworks;

@SuppressWarnings("serial")
public class SetAccountInfoPanel extends JPanel {

	private static final Dimension FIELD_DIMENSION = new Dimension(500, 20);
	private final JLayer<JPanel> jlayer = new JLayer<>();
	private final WaitLayerUI wuli = new WaitLayerUI();
	private final JComboBox<String> acc_combo_box = new JComboBox<>();

	
	
	private CrptoNetworks network;
	private String account;
	private byte[] public_key;

	public SetAccountInfoPanel() {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
		add(jlayer, BorderLayout.CENTER);
		var _panel = new JPanel(new FlowLayout());
		jlayer.setView(_panel);
		jlayer.setUI(wuli);
		var panel_1 = new JPanel(new GridBagLayout());
		_panel.add(panel_1);
		var label_1 = new JLabel("Account");
		panel_1.add(label_1, newGridConst(0, 0, 3, 17));
		acc_combo_box.setPreferredSize(new Dimension(300, 20));
		panel_1.add(acc_combo_box, newGridConst(0, 1, 3));

		var label_4 = new JLabel("Name");
		panel_1.add(label_4, newGridConst(0, 2, 3, 17));
		var rcv_field = new JTextField();
		rcv_field.setPreferredSize(FIELD_DIMENSION);
		panel_1.add(rcv_field, newGridConst(0, 3, 5));

		var label_5 = new JLabel("Description");
		panel_1.add(label_5, newGridConst(0, 4, 3, 17));
		var amt_field = new JTextField();
		amt_field.setPreferredSize(FIELD_DIMENSION);
		panel_1.add(amt_field, newGridConst(0, 5, 5));

		var label_6 = new JLabel("Fee");
		panel_1.add(label_6, newGridConst(0, 6, 3, 17));
		var fee_field = new JTextField();
		var fee_panel = new JPanel(new GridLayout(1, 0));
		fee_panel.setPreferredSize(FIELD_DIMENSION);
		var fee_button_panel = new JPanel(new GridLayout(1, 0));
		var fee_btn_cheap = new JToggleButton("Cheap");
		var fee_btn_stand = new JToggleButton("Standard");
		var fee_btn_priot = new JToggleButton("Priority");
		var btn_gp_0 = new ButtonGroup();
		Stream.of(fee_btn_cheap, fee_btn_stand, fee_btn_priot).forEach(btn_gp_0::add);
		Stream.of(fee_btn_cheap, fee_btn_stand, fee_btn_priot).forEach(fee_button_panel::add);

		fee_panel.add(fee_field);
		fee_panel.add(fee_button_panel);
		panel_1.add(fee_panel, newGridConst(0, 7, 5));

		var send_btn = new JButton(MyToolbar.getIcon("paper-plane-solid.svg"));
		panel_1.add(send_btn, new GridBagConstraints(4, 0, 1, 3, 0, 0, 10, 1, new Insets(5, 5, 5, 0), 0, 0));

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
