package hk.zdl.crypto.pearlet.component.settings.wizard;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import hk.zdl.crypto.pearlet.ui.UIUtil;
import se.gustavkarlsson.gwiz.AbstractWizardPage;

public class ChooseNetworkType extends AbstractWizardPage {

	private static final long serialVersionUID = -6080647905411931600L;
	private EnterNetworkDetail next;
	private JRadioButton op_1 = new JRadioButton("Burst Variant", true);
	private JRadioButton op_2 = new JRadioButton("Ethereum", false);
	private ButtonGroup group = new ButtonGroup();

	public ChooseNetworkType() {
		super(new GridBagLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Choose Network Type", TitledBorder.LEFT, TitledBorder.TOP,
				new Font("Arial Black", Font.PLAIN, (int) (getFont().getSize() * 1.5))));
		var label_1 = new JLabel(UIUtil.getStretchIcon("icon/" + "blockchain-dot-com-svgrepo-com.svg", 64, 64));
		var label_2 = new JLabel(UIUtil.getStretchIcon("icon/" + "ethereum-svgrepo-com-2.svg", 64, 64));
		group.add(op_1);
		group.add(op_2);
		add(label_1, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(label_2, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(op_1, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		add(op_2, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

	}

	public void setNextPage(EnterNetworkDetail next) {
		this.next = next;
	}

	public String getSelected() {
		if (op_1.isSelected()) {
			return "Burst Variant";
		} else {
			return "Ethereum";
		}
	}

	@Override
	protected AbstractWizardPage getNextPage() {
		next.setNetworkType(getSelected());
		return next;
	}

	@Override
	protected boolean isCancelAllowed() {
		return true;
	}

	@Override
	protected boolean isPreviousAllowed() {
		return false;
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
