package hk.zdl.crypto.pearlet.component.miner;

import javax.swing.JTabbedPane;

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
	}

}
