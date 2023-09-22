package hk.zdl.crypto.pearlet.component.blocks;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import org.json.JSONObject;

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.util.CryptoUtil;

public class BlockCache {

	private static final Map<Entry, JSONObject> map = new WeakHashMap<>(5000);

	static class Entry {
		private final CryptoNetwork nw;
		private final String block_id;

		Entry(CryptoNetwork nw, String block_id) {
			super();
			this.nw = nw;
			this.block_id = block_id;
		}

		@Override
		public int hashCode() {
			return Objects.hash(block_id, nw);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Entry other = (Entry) obj;
			return Objects.equals(block_id, other.block_id) && Objects.equals(nw, other.nw);
		}

	}

	static final synchronized JSONObject getSignumBlock(CryptoNetwork nw, String block_id) throws Exception {
		var e = new Entry(nw, block_id);
		var o = map.get(e);
		if (o == null) {
			o = CryptoUtil.getSignumBlock(nw, block_id);
			map.put(e, o);
		} else {
//			System.out.println("CACHE HIT!!!!!");
		}
		return o;
	}
}
