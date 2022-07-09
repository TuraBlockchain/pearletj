package hk.zdl.crypto.pearlet.ens;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;

import hk.zdl.crypto.pearlet.util.CryptoUtil;

public class ENSLookup {

	private static final Map<String, String> cache = Collections.synchronizedMap(new TreeMap<>());
	private static EnsResolver r = null;

	public static final String lookup(String str) {
		if (cache.containsKey(str)) {
			return cache.get(str);
		}
		buildResolverIfAbsent();
		if (r == null) {
			return null;
		}
		String result = null;
		try {
			result = r.resolve(str);
		} catch (Throwable e) {
		}
		if (result != null) {
			cache.put(str, result);
			return result;
		}
		return null;
	}
	
	public static final String reverse_lookup(String str){
		if (cache.containsKey(str)) {
			return cache.get(str);
		}
		buildResolverIfAbsent();
		if (r == null) {
			return null;
		}
		String result = null;
		try {
			result = r.reverseResolve(str);
		} catch (Throwable e) {
		}
		if (result != null) {
			cache.put(str, result);
			return result;
		}
		return null;
	}

	private static final synchronized void buildResolverIfAbsent() {
		if (r == null) {
			Optional<Web3j> o_j = CryptoUtil.getWeb3j();
			if (o_j.isPresent()) {
				r = new EnsResolver(o_j.get());
			}
		}
	}
}
