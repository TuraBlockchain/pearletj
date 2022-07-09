package hk.zdl.crypto.pearlet.tx_history_query;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import hk.zdl.crypto.pearlet.component.event.TxHistoryEvent;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EtherTxQuery {

	private static final String _key = "ckey_1f90a230af9d4076a77908c9fa0";
	private final OkHttpClient client = new OkHttpClient();

	public void queryTxHistory(String address) throws Exception {
		Request request = new Request.Builder().url("https://api.covalenthq.com/v1/1/address/" + address + "/transactions_v2/?key=" + _key).build();
		Response response = client.newCall(request).execute();
		JSONObject jobj = new JSONObject(new JSONTokener(response.body().byteStream()));
		JSONArray items = jobj.getJSONObject("data").getJSONArray("items");
		for(int i=0;i<items.length();i++) {
			jobj = items.getJSONObject(i);
			EventBus.getDefault().post(new TxHistoryEvent<JSONObject>(CrptoNetworks.WEB3J, TxHistoryEvent.Type.INSERT, jobj));
		}
	}
}
