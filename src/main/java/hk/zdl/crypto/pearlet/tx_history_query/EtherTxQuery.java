package hk.zdl.crypto.pearlet.tx_history_query;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.json.JSONTokener;

import hk.zdl.crypto.pearlet.component.event.TxHistoryEvent;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class EtherTxQuery {

	private static final String _key = Util.getProp().get("covalenthq_apikey");
	private final OkHttpClient client = new OkHttpClient();

	public void queryTxHistory(String address) throws Exception {
		var request = new Request.Builder().url("https://api.covalenthq.com/v1/1/address/" + address + "/transactions_v2/?key=" + _key).build();
		var response = client.newCall(request).execute();
		var jobj = new JSONObject(new JSONTokener(response.body().byteStream()));
		var items = jobj.getJSONObject("data").getJSONArray("items");
		for(int i=0;i<items.length();i++) {
			jobj = items.getJSONObject(i);
			EventBus.getDefault().post(new TxHistoryEvent<JSONObject>(CrptoNetworks.WEB3J, TxHistoryEvent.Type.INSERT, jobj));
		}
		response.body().byteStream().close();
		response.close();
	}
}
