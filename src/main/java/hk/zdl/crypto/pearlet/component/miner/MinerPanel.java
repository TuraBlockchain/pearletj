package hk.zdl.crypto.pearlet.component.miner;

import java.util.ResourceBundle;

import javax.swing.JLayer;
import javax.swing.JTabbedPane;

import hk.zdl.crypto.pearlet.component.JoinPoolPanel;
import hk.zdl.crypto.pearlet.component.commit.CommitPanel;
import hk.zdl.crypto.pearlet.component.miner.local.LocalMinerPanel;
import hk.zdl.crypto.pearlet.component.miner.remote.MinerExplorePane;
import hk.zdl.crypto.pearlet.ui.CloseableTabbedPaneLayerUI;
import hk.zdl.crypto.pearlet.util.Util;

public class MinerPanel extends JTabbedPane {

	private static final long serialVersionUID = 2889382640387190885L;
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();

	public MinerPanel() {
		addTab(rsc_bdl.getString("MINER.TAB.LOCAL"),new JLayer<JTabbedPane>(new LocalMinerPanel(), new CloseableTabbedPaneLayerUI()));
		addTab(rsc_bdl.getString("MINER.TAB.REMOTE"),new JLayer<JTabbedPane>(new MinerExplorePane(), new CloseableTabbedPaneLayerUI()));
		addTab(rsc_bdl.getString("MINER.TAB.STAK"), new CommitPanel());
		addTab(rsc_bdl.getString("MINER.TAB.POOL"), new JoinPoolPanel());
	}

}
