package hk.zdl.crypto.pearlet.component.miner;

import javax.swing.JTabbedPane;

public class LocalMinerPanel extends JTabbedPane {

	private static final long serialVersionUID = -5549092062588608169L;

	public LocalMinerPanel() {
	}

	@Override
	public void removeTabAt(int index) {
		if (index == 0) {
			return;
		}
		super.removeTabAt(index);
	}
}
