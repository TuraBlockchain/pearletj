package com.cloudflare.jep418;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.json.JSONTokener;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CloudflareInetAddressResolver implements InetAddressResolver {
	private static final OkHttpClient _client = new OkHttpClient.Builder().addInterceptor(chain -> {
		Request request = chain.request();
		Request authenticatedRequest = request.newBuilder().header("Accept", "application/dns-json").build();
		return chain.proceed(authenticatedRequest);
	}).build();

	@Override
	public Stream<InetAddress> lookupByName(String host, LookupPolicy lookupPolicy) throws UnknownHostException {
		if(host.equals("cloudflare-dns.com")) {
			return Stream.of(parse("104.16.248.249"));
		}
		try {
			var request = new Request.Builder().url("https://1.1.1.1/dns-query?name=" + host + "&type=1").build();
			var response = _client.newCall(request).execute();
			var jobj = new JSONObject(new JSONTokener(response.body().byteStream()));
			jobj = jobj.getJSONArray("Answer").getJSONObject(0);
			var data = jobj.getString("data");
			return Stream.of(parse(data));
		} catch (Exception e) {
			throw new UnknownHostException();
		}
	}

	@Override
	public String lookupByAddress(byte[] addr) {
		throw new UnsupportedOperationException();
	}

	private static final InetAddress parse(String str) throws UnknownHostException {
		var s_arr = str.split("[.]");
		byte[] b_arr = new byte[4];
		for (int i = 0; i < 4; i++) {
			b_arr[i] = (byte) Integer.parseInt(s_arr[i]);
		}
		return InetAddress.getByAddress(b_arr);
	}
}
