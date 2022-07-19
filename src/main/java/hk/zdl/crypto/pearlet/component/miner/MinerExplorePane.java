package hk.zdl.crypto.pearlet.component.miner;

import java.awt.FontMetrics;

import javax.swing.JTabbedPane;

import com.formdev.flatlaf.ui.FlatTabbedPaneUI;

public class MinerExplorePane extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4709359665652317477L;

	public MinerExplorePane() {
		add("Start", new StartPanel(this));
		addChangeListener(e -> {
			if (getTabCount() == 0) {
				add("Start", new StartPanel(this));
			}
		});
		setUI(new FlatTabbedPaneUI() {

			@Override
			protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
				return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 32;
			}

		});
	}

}
