package hk.zdl.crypto.pearlet.misc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.component.event.BalanceUpdateEvent;
import hk.zdl.crypto.pearlet.component.event.TxHistoryEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.ens.ENSLookup;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.response.Transaction;
import signumj.response.attachment.AccountInfoAttachment;

public class AccountTableModel extends AbstractTableModel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4807088125271894490L;
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	private static final List<String> columnNames = Stream.of("ID", "NW", "ADR", "BAL", "ALA", "DSC").map(s -> rsc_bdl.getString("SETTINGS.ACCOUNT.COLUMN." + s)).toList();
	private final Timer mTimer = new Timer((int) TimeUnit.SECONDS.toMillis(30), this);
	private Map<List<Integer>, Object> sparse = new HashMap<>();
	private List<Record> accounts = Arrays.asList();

	public AccountTableModel() {
		mTimer.start();
	}

	@Override
	public int getRowCount() {
		return accounts.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames.get(columnIndex);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		sparse.put(Arrays.asList(rowIndex, columnIndex), aValue);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		var r = accounts.get(rowIndex);
		if (columnIndex == 0) {
			return r.getInt("ID");
		} else if (columnIndex == 1) {
			var i = r.getInt("NWID");
			return i == null ? null : MyDb.get_networks().stream().filter(o -> o.getId() == i).findAny().orElse(null);
		} else if (columnIndex == 2) {
			var o = r.getStr("ADDRESS");
			var b = r.getBytes("PRIVATE_KEY");
			if (b == null || b.length == 0)
				o += ",watch";
			return o;
		} else {
			List<Integer> l = Arrays.asList(rowIndex, columnIndex);
			if (sparse.containsKey(l)) {
				return sparse.get(l);
			}
		}
		return null;
	}

	public void setAccounts(List<Record> accounts) {
		sparse.clear();
		int old_size = this.accounts.size();
		int new_size = accounts.size();
		this.accounts = accounts;
		if (new_size == old_size) {
			fireTableRowsUpdated(0, new_size);
		} else {
			fireTableRowsDeleted(0, old_size);
			fireTableRowsInserted(0, new_size);
		}
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public synchronized void onMessage(AccountListUpdateEvent e) {
		var l = MyDb.getAccounts();
		var arr_a = l.stream().mapToInt(o -> o.getInt("ID")).toArray();
		var arr_b = accounts.stream().mapToInt(o -> o.getInt("ID")).toArray();
		if (Arrays.equals(arr_a, arr_b)) {
			return;
		}
		setAccounts(l);
		var nws = MyDb.get_networks();
		for (int i = 0; i < l.size(); i++) {
			var r = l.get(i);
			if (r.getInt("NWID") == null)
				continue;
			var nw = nws.stream().filter(o -> o.getId() == r.getInt("NWID")).findAny().get();
			var address = r.getStr("ADDRESS");
			Util.submit(new BalanceQuery(nw, address));
			if (nw.isWeb3J()) {
				Util.submit(new ENSQuery(address, i));
			}
		}
	}

	private final class ENSQuery implements Callable<Void> {
		private final String address;
		private final int i;

		public ENSQuery(String address, int i) {
			this.address = address;
			this.i = i;
		}

		@Override
		public Void call() throws Exception {
			String str = ENSLookup.reverse_lookup(address);
			if (str != null) {
				setValueAt(str, i, 4);
				fireTableRowsUpdated(i, i);
			}
			return null;
		}

	}

	private final class BalanceQuery implements Callable<Void> {
		private final CryptoNetwork nw;
		private final String address;

		public BalanceQuery(CryptoNetwork nw, String address) {
			this.nw = nw;
			this.address = address;
		}

		@Override
		public Void call() throws Exception {
			if (nw == null || address == null) {
				return null;
			}
			var bal = CryptoUtil.getBalance(nw, address);
			EventBus.getDefault().post(new BalanceUpdateEvent(nw, address, bal));
			return null;
		}
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(TxHistoryEvent<?> e) {
		if (e.type.equals(TxHistoryEvent.Type.INSERT)) {
			if (e.network.isBurst()) {
				Transaction tx = (Transaction) e.data;
				if (tx.getType() == 1 && tx.getSubtype() == 5) {
					var address = tx.getSender().getRawAddress();
					AccountInfoAttachment atta = (AccountInfoAttachment) tx.getAttachment();
					var aliases = atta.getName();
					var desc = atta.getDescription();
					for (int i = 0; i < accounts.size(); i++) {
						var addr = getValueAt(i, 2).toString();
						var raw_addr = addr.substring(addr.indexOf('-') + 1);
						if (raw_addr.equals(address)) {
							setValueAt(aliases, i, 4);
							setValueAt(desc, i, 5);
							fireTableRowsUpdated(i, i);
							ENSLookup.put(addr, aliases);
						}
					}
				}
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(BalanceUpdateEvent e) {
		for (var i = 0; i < getRowCount(); i++) {
			var r = accounts.get(i);
			if (r.getInt("NWID") == e.getNetwork().getId()) {
				if (r.getStr("ADDRESS").equals(e.getAddress())) {
					setValueAt(e.getBalance(), i, 3);
					fireTableRowsUpdated(i, i);
				}
			}
		}
	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		for (int i = 0; i < getRowCount(); i++) {
			var nw = (CryptoNetwork) getValueAt(i, 1);
			var adr = accounts.get(i).getStr("ADDRESS");
			var q = new BalanceQuery(nw, adr);
			Util.submit(q);
		}
	}

}
