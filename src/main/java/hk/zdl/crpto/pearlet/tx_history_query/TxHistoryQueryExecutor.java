package hk.zdl.crpto.pearlet.tx_history_query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crpto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crpto.pearlet.component.event.TxHistoryEvent;
import hk.zdl.crpto.pearlet.util.CrptoNetworks;

public class TxHistoryQueryExecutor {

	private final List<MyThread> threads = Collections.synchronizedList(new ArrayList<>());

	public TxHistoryQueryExecutor() {
		EventBus.getDefault().register(this);
	}

	@SuppressWarnings({ "removal", "unchecked", "rawtypes" })
	@Subscribe(threadMode = ThreadMode.ASYNC)
	public synchronized void onMessage(AccountChangeEvent e) {
		EventBus.getDefault().post(new TxHistoryEvent(e.network, TxHistoryEvent.Type.START, null));
		for (MyThread t : threads) {
			try {
				t.stop();
			} catch (Exception x) {
			}
		}
		threads.clear();
		MyThread t = new MyThread(e.network, e.account);
		t.start();
		threads.add(t);
	}

	private final class MyThread extends Thread {
		CrptoNetworks network;
		String account;

		public MyThread(CrptoNetworks network, String account) {
			this.network = network;
			this.account = account;
			setPriority(MIN_PRIORITY);
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				switch (network) {
				case ROTURA:
				case SIGNUM:
					new TxHistoryQueryImpl(network).queryTxHistory(account);
					System.out.println("MyThread::run()");
					break;
				case WEB3J:
					break;
				default:
					break;

				}
				send_finish_msg();
			} catch (InterruptedException e) {
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
				send_finish_msg();
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void send_finish_msg() {
			EventBus.getDefault().post(new TxHistoryEvent(network, TxHistoryEvent.Type.FINISH, null));
		}

	}
}
