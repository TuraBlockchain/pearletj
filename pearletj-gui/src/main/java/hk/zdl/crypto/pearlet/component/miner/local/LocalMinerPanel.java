package hk.zdl.crypto.pearlet.component.miner.local;

import javax.swing.JTabbedPane;

public class LocalMinerPanel extends JTabbedPane {

	private static final long serialVersionUID = -5549092062588608169L;

	public LocalMinerPanel() {
		addTab("Start", new StartPanel(this));
	}

	@Override
	public void removeTabAt(int index) {
		if (index == 0) {
			return;
		}else {
			var c = getComponentAt(index);
			if(c instanceof MinerPanel) {
				((MinerPanel)c).destroyForcibly();
			}
		}
		super.removeTabAt(index);
	}
}
