package hk.zdl.crypto.pearlet.ds;

import org.json.JSONObject;

import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import signumj.entity.response.Asset;

public class AltTokenWrapper {
	public final CrptoNetworks network;
	public JSONObject jobj;
	public Asset asset;
	public AltTokenWrapper(CrptoNetworks network) {
		this.network = network;
	}
	public AltTokenWrapper(CrptoNetworks network, Asset asset) {
		this.network = network;
		this.asset = asset;
	}
	public AltTokenWrapper(CrptoNetworks network, JSONObject jobj) {
		this.network = network;
		this.jobj = jobj;
	}

}
