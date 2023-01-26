package hk.zdl.crypto.pearlet.component.miner.remote;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class MyHC {

	private static final CloseableHttpClient httpclient = HttpClients.createMinimal();

	public static CloseableHttpClient getHttpclient() {
		return httpclient;
	}
}
