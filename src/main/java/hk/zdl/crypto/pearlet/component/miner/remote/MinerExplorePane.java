package hk.zdl.crypto.pearlet.component.miner.remote;

import javax.swing.JTabbedPane;

import hk.zdl.crypto.pearlet.util.Util;

public class MinerExplorePane extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4709359665652317477L;

	public MinerExplorePane() {
		add(Util.getResourceBundle().getString("MINER.REMOTE.SEARCH_1"), new StartPanel(this));
	}

	@Override
	public void removeTabAt(int index) {
		if (index == 0) {
			return;
		}
		super.removeTabAt(index);
	}

}
