package hk.zdl.crypto.pearlet.component.miner.local;

import javax.swing.JTabbedPane;

import hk.zdl.crypto.pearlet.util.Util;

public class LocalMinerPanel extends JTabbedPane {

	private static final long serialVersionUID = -5549092062588608169L;

	public LocalMinerPanel() {
		addTab(Util.getResourceBundle().getString("MINER.TAB.LOCAL.START"), new StartPanel(this));
	}

	@Override
	public void removeTabAt(int index) {
		if (index == 0) {
			return;
		}else {
			var c = getComponentAt(index);
			if(c instanceof MinerPanel) {
				((MinerPanel)c).stop();
			}
		}
		super.removeTabAt(index);
	}
}
