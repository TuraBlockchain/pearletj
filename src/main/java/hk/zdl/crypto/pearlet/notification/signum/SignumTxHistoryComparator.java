package hk.zdl.crypto.pearlet.notification.signum;
import java.util.Comparator;

import org.json.JSONObject;

public class SignumTxHistoryComparator implements Comparator<JSONObject> {

	@Override
	public int compare(JSONObject o1, JSONObject o2) {
		int i1 = o1.getInt("block");
		int i2 = o1.getInt("block");
		int i3 = Integer.compare(i1, i2);
		if (i3 == 0) {
			var s1 = o1.getString("fullHash");
			var s2 = o2.getString("fullHash");
			return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
		} else {
			return i3;
		}
	}

}
