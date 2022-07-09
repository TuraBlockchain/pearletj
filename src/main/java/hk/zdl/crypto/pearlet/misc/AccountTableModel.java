package hk.zdl.crypto.pearlet.misc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.table.AbstractTableModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crypto.pearlet.component.event.TxHistoryEvent;
import static hk.zdl.crypto.pearlet.util.CrptoNetworks.*;

import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.response.Transaction;
import signumj.response.attachment.AccountInfoAttachment;

@SuppressWarnings("serial")
public class AccountTableModel extends AbstractTableModel {

	private static final List<String> columnNames = Arrays.asList("Id", "Network", "Address", "Balance", "Alias", "Description");
	private Map<List<Integer>, Object> sparse = new HashMap<>();
	private List<Record> accounts = Arrays.asList();

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
		Record r = accounts.get(rowIndex);
		if (columnIndex == 0) {
			return r.get("ID");
		} else if (columnIndex == 1) {
			var o = r.get("NETWORK");
			var b = r.getBytes("PRIVATE_KEY");
			if (b == null || b.length == 0)
				o += " (watch)";
			return o;
		} else if (columnIndex == 2) {
			var b = r.getBytes("PRIVATE_KEY");
			if (b == null || b.length == 0) {
				return r.getStr("ADDRESS");
			}
			try {
				CrptoNetworks nw = CrptoNetworks.valueOf(r.getStr("NETWORK"));
				return CryptoUtil.getAddress(nw, r.getBytes("PUBLIC_KEY"));
			} catch (Exception e) {
				return null;
			}
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
	public void onMessage(AccountListUpdateEvent e) {
		setAccounts(e.getAccounts());
		for (int i = 0; i < e.getAccounts().size(); i++) {
			Record r = e.getAccounts().get(i);
			CrptoNetworks nw = CrptoNetworks.valueOf(r.getStr("NETWORK"));
			String address = r.getStr("ADDRESS");
			Util.submit(new MyCallable(nw, address, i));
		}
	}

	private final class MyCallable implements Callable<Void> {
		private final CrptoNetworks nw;
		private final String address;
		private final int i;

		public MyCallable(CrptoNetworks nw, String address, int i) {
			super();
			this.nw = nw;
			this.address = address;
			this.i = i;
		}

		@Override
		public Void call() throws Exception {
			String balance = CryptoUtil.getBalance(nw, address).stripTrailingZeros().toString();
			setValueAt(balance, i, 3);
			fireTableRowsUpdated(i, i);
			return null;
		}
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(TxHistoryEvent<?> e) {
		if (e.type.equals(TxHistoryEvent.Type.INSERT)) {
			if (Arrays.asList(SIGNUM, ROTURA).contains(e.network)) {
				Transaction tx = (Transaction) e.data;
				if (tx.getType() == 1 && tx.getSubtype() == 5) {
					String address = tx.getSender().getFullAddress();
					AccountInfoAttachment atta = (AccountInfoAttachment) tx.getAttachment();
					String aliases = atta.getName();
					String desc = atta.getDescription();
					for (int i = 0; i < accounts.size(); i++) {
						Record r = accounts.get(i);
						CrptoNetworks nw = CrptoNetworks.valueOf(r.getStr("NETWORK"));
						String addr = CryptoUtil.getAddress(nw, r.getBytes("PUBLIC_KEY"));
						if (addr.equals(address)) {
							setValueAt(aliases, i, 4);
							setValueAt(desc, i, 5);
							fireTableRowsUpdated(i, i);
						}
					}
				}
			}
		}
	}

}
