package hk.zdl.crpto.pearlet.misc;

import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crpto.pearlet.component.event.AccountListUpdateEvent;
import hk.zdl.crpto.pearlet.util.CryptoUtil;

@SuppressWarnings("serial")
public class AccountTableModel extends AbstractTableModel {

	private static final List<String> columnNames = Arrays.asList("Id", "Network", "Address"/** , "Alias", "Balance", "Description" **/
	);
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
	public Object getValueAt(int rowIndex, int columnIndex) {
		Record r = accounts.get(rowIndex);
		if (columnIndex == 0) {
			return r.get("ID");
		} else if (columnIndex == 1) {
			var o = r.get("NETWORK");
			var b = r.getBytes("PRIVATE_KEY");
			if (b.length == 0)
				o += " (watch)";
			return o;
		} else if (columnIndex == 2) {
			return CryptoUtil.getAddress(r.getStr("NETWORK"), r.getBytes("PUBLIC_KEY"));
		}
		return null;
	}

	public void setAccounts(List<Record> accounts) {
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
	}

}
