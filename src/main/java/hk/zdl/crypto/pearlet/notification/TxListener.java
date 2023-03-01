package hk.zdl.crypto.pearlet.notification;

import org.json.JSONObject;

public interface TxListener {
	public void transcationReceived(JSONObject jobj);
}
