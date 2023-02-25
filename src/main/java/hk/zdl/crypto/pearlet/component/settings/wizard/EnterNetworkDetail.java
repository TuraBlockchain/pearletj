package hk.zdl.crypto.pearlet.component.settings.wizard;

import java.awt.CardLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import se.gustavkarlsson.gwiz.AbstractWizardPage;

public class EnterNetworkDetail extends AbstractWizardPage {

	private static final long serialVersionUID = 1085301247361719995L;

	public EnterNetworkDetail() {
		super(new CardLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Provide Network Detail", TitledBorder.LEFT, TitledBorder.TOP,
				new Font("Arial Black", Font.PLAIN, (int) (getFont().getSize() * 1.5))));
		var burst_panel = new JPanel();
		var eth_panel = new JPanel();
		add(burst_panel, "Burst Variant");
		add(eth_panel, "Ethereum");
		burst_panel.add(new JLabel("burst_panel"));
		eth_panel.add(new JLabel("eth_panel"));
		
	}

	protected void setNetworkType(String type) {
		((CardLayout) getLayout()).show(this, type);
		
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
