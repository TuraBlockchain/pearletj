package hk.zdl.crypto.pearlet.component.miner;

import javax.swing.JLayer;
import javax.swing.JTabbedPane;

import hk.zdl.crypto.pearlet.component.JoinPoolPanel;
import hk.zdl.crypto.pearlet.component.commit.CommitPanel;
import hk.zdl.crypto.pearlet.component.miner.local.LocalMinerPanel;
import hk.zdl.crypto.pearlet.component.miner.remote.MinerExplorePane;
import hk.zdl.crypto.pearlet.ui.CloseableTabbedPaneLayerUI;

public class MinerPanel extends JTabbedPane {

	private static final long serialVersionUID = 2889382640387190885L;

	public MinerPanel() {
		addTab("Local Miner",new JLayer<JTabbedPane>(new LocalMinerPanel(), new CloseableTabbedPaneLayerUI()));
		addTab("Remote Miner",new JLayer<JTabbedPane>(new MinerExplorePane(), new CloseableTabbedPaneLayerUI()));
		addTab("Commitment", new CommitPanel());
		addTab("Join Pool", new JoinPoolPanel());
	}

}
