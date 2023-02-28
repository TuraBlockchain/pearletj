package hk.zdl.crypto.pearlet.ds;

import org.json.JSONObject;

import signumj.entity.response.Asset;

public class AltTokenWrapper {
	public final CryptoNetwork network;
	public JSONObject jobj;
	public Asset asset;
	public AltTokenWrapper(CryptoNetwork network) {
		this.network = network;
	}
	public AltTokenWrapper(CryptoNetwork network, Asset asset) {
		this.network = network;
		this.asset = asset;
	}
	public AltTokenWrapper(CryptoNetwork network, JSONObject jobj) {
		this.network = network;
		this.jobj = jobj;
	}

}
