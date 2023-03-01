package hk.zdl.crypto.pearlet.util;

import java.util.concurrent.Callable;

import hk.zdl.crypto.pearlet.notification.signum.SignumAccountsMonitor;
import hk.zdl.crypto.pearlet.persistence.MyDb;

public class NWMon implements Callable<Void> {

	@Override
	public Void call() throws Exception {
		for (var n : MyDb.get_networks()) {
			if (n.isBurst()) {
				new SignumAccountsMonitor(n);
			} else if (n.isWeb3J()) {

			}
		}
		return null;
	}

}
