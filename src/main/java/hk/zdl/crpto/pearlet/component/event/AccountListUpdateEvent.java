package hk.zdl.crpto.pearlet.component.event;

import java.util.List;

import com.jfinal.plugin.activerecord.Record;

public class AccountListUpdateEvent {

	private final List<Record> accounts;

	public AccountListUpdateEvent(List<Record> accounts) {
		this.accounts = accounts;
	}

	public List<Record> getAccounts() {
		return accounts;
	}
}
