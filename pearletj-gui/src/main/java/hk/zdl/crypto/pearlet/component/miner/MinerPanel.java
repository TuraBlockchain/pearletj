package hk.zdl.crypto.pearlet.component.miner;

import javax.swing.JLayer;
import javax.swing.JTabbedPane;

import hk.zdl.crypto.pearlet.component.miner.local.LocalMinerPanel;
import hk.zdl.crypto.pearlet.component.miner.remote.MinerExplorePane;
import hk.zdl.crypto.pearlet.ui.CloseableTabbedPaneLayerUI;

public class MinerPanel extends JTabbedPane {

	private static final long serialVersionUID = 2889382640387190885L;

	public MinerPanel() {
		addTab("Local",new JLayer<JTabbedPane>(new LocalMinerPanel(), new CloseableTabbedPaneLayerUI()));
		addTab("Remote",new JLayer<JTabbedPane>(new MinerExplorePane(), new CloseableTabbedPaneLayerUI()));
	}

}
