package hk.zdl.crypto.pearlet.component.miner.remote;

import java.net.http.HttpClient;

public class ClientUpdateEvent {

	public final String base_path;
	public final HttpClient client;

	public ClientUpdateEvent(String base_path, HttpClient client) {
		super();
		this.base_path = base_path;
		this.client = client;
	}

	@Override
	public String toString() {
		return "ClientUpdateEvent [base_path=" + base_path + ", client=" + client + "]";
	}

}
