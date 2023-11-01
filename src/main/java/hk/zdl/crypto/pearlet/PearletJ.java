package hk.zdl.crypto.pearlet;

import java.awt.Taskbar;
import java.awt.Taskbar.Feature;
import java.io.File;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.derby.shared.common.error.StandardException;
import org.greenrobot.eventbus.EventBus;

import hk.zdl.crypto.pearlet.component.MainFrame;
import hk.zdl.crypto.pearlet.component.event.WalletLockEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.laf.MyUIManager;
import hk.zdl.crypto.pearlet.lock.WalletLock;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.tx_history_query.TxHistoryQueryExecutor;
import hk.zdl.crypto.pearlet.ui.AquaMagic;
import hk.zdl.crypto.pearlet.ui.GnomeMagic;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.util.NWMon;
import hk.zdl.crypto.pearlet.util.Util;

public class PearletJ {

	public static void main(String[] args) throws Throwable {
		AquaMagic.do_trick();
		GnomeMagic.do_trick();
		var app_icon = ImageIO.read(Util.getResource("app_icon.png"));
		if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Feature.ICON_IMAGE)) {
			Taskbar.getTaskbar().setIconImage(app_icon);
		}
		UIUtil.printVersionOnSplashScreen();
		MyUIManager.setLookAndFeel();
		var db_empty = is_db_empty();
		try {
			System.setProperty("derby.system.home", Files.createTempDirectory(null).toFile().getAbsolutePath());
			MyDb.create_missing_tables();
		} catch (Throwable x) {
			while (x.getCause() != null && x.getCause() != x) {
				x = x.getCause();
			}
			var msg = x.getLocalizedMessage();
			if (x.getClass().equals(StandardException.class)) {
				if (((StandardException) x).getSQLState().equals("XSDB6")) {
					msg = "Cannot run multi instances concurrently!";
				}
			}
			var pane = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE);
			var dlg = pane.createDialog(null, "Error");
			dlg.setIconImage(app_icon);
			dlg.setVisible(true);
			pane.getValue();
			System.exit(1);
		}
		if (db_empty) {
			create_default_networks();
		}
		SwingUtilities.invokeLater(() -> new MainFrame(Util.getProp().get("appName"), app_icon));
		SwingUtilities.invokeLater(() -> EventBus.getDefault().post(new WalletLockEvent(WalletLock.hasPassword() ? WalletLockEvent.Type.LOCK : WalletLockEvent.Type.UNLOCK)));
		new NWMon();
		new TxHistoryQueryExecutor();
	}

	private static void create_default_networks() throws Exception {
		var jarr = Util.get_predefined_networks();
		for (var i = 0; i < jarr.length(); i++) {
			var jobj = jarr.getJSONObject(i);
			if (jobj.optBoolean("add by default")) {
				var new_network = new CryptoNetwork();
				new_network.setName(jobj.getString("networkName"));
				new_network.setUrl(jobj.getString("server url"));
				new_network.setType(CryptoNetwork.Type.BURST);
				MyDb.insert_network(new_network);
			}
		}
	}

	private static boolean is_db_empty() throws Exception {
		var db_path = new File(Util.getUserDataDir()).toPath();
		if (!Files.exists(db_path)) {
			return true;
		} else if (Files.list(db_path).filter(p -> Files.isRegularFile(p) || Files.isDirectory(p)).count() < 1) {
			return true;
		}
		return false;
	}

}
