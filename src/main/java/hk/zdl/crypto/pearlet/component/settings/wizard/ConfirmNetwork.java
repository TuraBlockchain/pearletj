package hk.zdl.crypto.pearlet.component.settings.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;

import hk.zdl.crypto.pearlet.component.MyStretchIcon;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import se.gustavkarlsson.gwiz.AbstractWizardPage;

public class ConfirmNetwork extends AbstractWizardPage {

	private static final long serialVersionUID = 4162653716596967302L;
	private static final int icon_size = 64;
	private final JLabel icon = new JLabel(UIUtil.getStretchIcon("icon/blockchain-dot-com-svgrepo-com.svg", icon_size, icon_size));
	private final JTextField name = new JTextField();
	private final JTextField address = new JTextField();
	private String type;

	public ConfirmNetwork() {
		super(new GridBagLayout());
		add(icon, new GridBagConstraints(0, 0, 1, 2, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(new JLabel("Name"), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(new JLabel("URL"), new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(name, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		add(address, new GridBagConstraints(2, 1, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		address.setEditable(false);
	}

	public void setNetWorkName(String str, boolean editable) {
		name.setText(str);
		name.setEditable(editable);
	}

	public String getNetworkName() {
		return name.getText();
	}

	public void setNetworkAddress(String str) {
		address.setText(str);
	}

	public String getNetworkAddress() {
		return address.getText();
	}

	public void setIcon(Icon icon) {
		if (icon instanceof ImageIcon) {
			var i = (ImageIcon) icon;
			setIcon(i.getImage());
		} else {
			this.icon.setIcon(icon);
		}
	}

	public void setIcon(Image img) {
		this.icon.setIcon(new MyStretchIcon(img, icon_size, icon_size));
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
		return false;
	}

	@Override
	protected boolean isFinishAllowed() {
		return true;
	}

}
