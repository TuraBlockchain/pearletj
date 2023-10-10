package hk.zdl.crypto.pearlet.component.miner.local;

import java.awt.TrayIcon.MessageType;
import java.time.Duration;

import hk.zdl.crypto.pearlet.component.settings.DisplaySettings;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class ForgeNortiWorker implements Runnable {

	private final CryptoNetwork network;
	private final String account;
	private boolean running = true;

	ForgeNortiWorker(CryptoNetwork network, String account) {
		super();
		this.network = network;
		this.account = account;
	}

	@Override
	public void run() {
		var timestamp = -1L;
		while (timestamp < 0 && running) {
			try {
				timestamp = CryptoUtil.getBlockchainStatus(network).getLong("time");
			} catch (Exception x) {
			}
		}
		while (running) {
			try {
				if (Util.getUserSettings().getBoolean(DisplaySettings.SNSM, false)) {
					var block_ids = CryptoUtil.getSignumBlockID(network, account, timestamp);
					var coin_name = CryptoUtil.getConstants(network).getString("valueSuffix");
					for (var i = 0; i < block_ids.length(); i++) {
						var jobj = CryptoUtil.getSignumBlock(network, block_ids.getString(i));
						var blockReward = jobj.getInt("blockReward");// Integer
						UIUtil.displayMessage("" + blockReward + " " + coin_name + " have gained", "with id " + account, MessageType.INFO);
						timestamp = Math.max(timestamp, jobj.getLong("timestamp"));
					}
				}
				Thread.sleep(Duration.ofSeconds(30).toMillis());
			} catch (Exception x) {
			}
		}
	}

	public void stop() {
		running = false;
	}

}
