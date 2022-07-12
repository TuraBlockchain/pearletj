package hk.zdl.crypto.pearlet.util;

import static hk.zdl.crypto.pearlet.util.CrptoNetworks.ROTURA;
import static hk.zdl.crypto.pearlet.util.CrptoNetworks.SIGNUM;
import static hk.zdl.crypto.pearlet.util.CrptoNetworks.WEB3J;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.ds.RoturaAddress;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import signumj.crypto.SignumCrypto;
import signumj.entity.EncryptedMessage;
import signumj.entity.SignumAddress;
import signumj.entity.SignumID;
import signumj.entity.SignumValue;
import signumj.entity.response.Account;
import signumj.entity.response.Asset;
import signumj.entity.response.Transaction;
import signumj.entity.response.http.BRSError;
import signumj.service.NodeService;

public class CryptoUtil {

	private static Web3j _web3j;

	private static final synchronized void build_web3j() {
		String base_url = get_server_url(WEB3J).get();
		if (!base_url.endsWith("/")) {
			base_url += "/";
		}
		Optional<Record> o = MyDb.get_webj_auth();
		if (o.isPresent()) {
			base_url += o.get().getStr("MYAUTH");
			var httpCredentials = okhttp3.Credentials.basic("", o.get().getStr("SECRET"));
			var client = new OkHttpClient.Builder().addInterceptor(chain -> {
				Request request = chain.request();
				Request authenticatedRequest = request.newBuilder().header("Authorization", httpCredentials).build();
				return chain.proceed(authenticatedRequest);
			}).build();
			_web3j = Web3j.build(new HttpService(base_url, client));
		}
	}

	static {
		RoturaAddress.getAddressPrefix();
	}

	public static final void clear_web3j() {
		_web3j = null;
	}

	public static final Optional<Web3j> getWeb3j() {
		if (_web3j == null) {
			build_web3j();
		}
		return _web3j == null ? Optional.empty() : Optional.of(_web3j);
	}

	public static final boolean isValidAddress(CrptoNetworks network, String address) {
		if (address == null || address.isBlank()) {
			return false;
		}
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			try {
				return SignumAddress.fromEither(address) != null;
			} catch (Exception e) {
				return false;
			}
		} else if (WEB3J.equals(network)) {
			return address.contains(".") || WalletUtils.isValidAddress(address);
		}
		return false;
	}

	public static final byte[] getPublicKeyFromAddress(CrptoNetworks network, String addr) {
		if (network.equals(SIGNUM)) {
			return SignumAddress.fromRs(addr).getPublicKey();
		}
		throw new UnsupportedOperationException();
	}

	public static final byte[] getPublicKey(CrptoNetworks network, String type, String text) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			if (type.equalsIgnoreCase("phrase")) {
				return SignumCrypto.getInstance().getPublicKey(text);
			}
		}
		throw new UnsupportedOperationException();
	}

	public static final byte[] getPrivateKey(CrptoNetworks network, String type, String text) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			if (type.equalsIgnoreCase("phrase")) {
				return SignumCrypto.getInstance().getPrivateKey(text);
			} else if (type.equalsIgnoreCase("base64")) {
				return Base64.decode(text);
			} else if (type.equals("HEX")) {
				return Hex.decode(text);
			}
		}
		throw new UnsupportedOperationException();
	}

	public static final byte[] getPublicKey(CrptoNetworks network, byte[] private_key) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			return SignumCrypto.getInstance().getPublicKey(private_key);
		}
		throw new UnsupportedOperationException();
	}

	public static final String getAddress(CrptoNetworks network, byte[] public_key) {
		if (network.equals(SIGNUM)) {
			return SignumCrypto.getInstance().getAddressFromPublic(public_key).getFullAddress();
		} else if (network.equals(ROTURA)) {
			return RoturaAddress.fromPublicKey(public_key).toString();
		} else if (WEB3J.equals(network)) {
			return "0x" + org.web3j.crypto.Keys.getAddress(org.web3j.utils.Numeric.toBigInt(public_key));
		}
		return null;
	}

	public static final Asset getAsset(CrptoNetworks network, String assetId) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			Optional<String> opt = get_server_url(network);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				try {
					return ns.getAsset(SignumID.fromLong(assetId)).blockingGet();
				} catch (IllegalArgumentException e) {
					throw e;
				}
			}
		}
		throw new UnsupportedOperationException();
	}

	public static final Account getAccount(CrptoNetworks network, String address) throws Exception {
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			Optional<String> opt = get_server_url(network);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				try {
					return ns.getAccount(SignumAddress.fromRs(address)).toFuture().get();
				} catch (IllegalArgumentException | InterruptedException | ExecutionException e) {
					throw e;
				}
			}
		}
		throw new UnsupportedOperationException();
	}

	public static final BigDecimal getBalance(CrptoNetworks network, String address) throws Exception {
		if (Arrays.asList(SIGNUM, ROTURA).contains(network)) {
			Optional<String> opt = get_server_url(network);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				try {
					return ns.getAccount(SignumAddress.fromRs(address)).toFuture().get().getBalance().toSigna();
				} catch (IllegalArgumentException | InterruptedException | ExecutionException e) {
					if (e.getCause() != null) {
						if (e.getCause().getClass().getName().equals("signumj.entity.response.http.BRSError")) {
							BRSError e1 = (BRSError) e.getCause();
							if (e1.getCode() == 5) {
								return new BigDecimal(0);
							}
						}
					}
					throw e;
				}
			}
		} else if (WEB3J.equals(network)) {
			if (getWeb3j().isPresent()) {
				try {
					BigInteger wei = getWeb3j().get().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
					BigDecimal eth = Convert.fromWei(new BigDecimal(wei), Convert.Unit.ETHER).stripTrailingZeros();
					return eth;
				} catch (IOException e) {
					return new BigDecimal("-1");
				}
			}else {
				try {
					String _key = Util.getProp().get("covalenthq_apikey");
					OkHttpClient client = new OkHttpClient();
					Request request = new Request.Builder().url("https://api.covalenthq.com/v1/1/address/" + address + "/balances_v2/?quote-currency=ETH&format=JSON&nft=false&no-nft-fetch=true&key=" + _key).build();
					Response response = client.newCall(request).execute();
					JSONObject jobj = new JSONObject(new JSONTokener(response.body().byteStream()));
					if(jobj.getBoolean("error")) {
						throw new Exception();
					}else {
						var items = jobj.getJSONObject("data").getJSONArray("items");
						for(int i=0;i<items.length();i++) {
							jobj = items.getJSONObject(i);
							if(jobj.getString("contract_name").equals("Ether")&&jobj.getString("contract_ticker_symbol").equals("ETH")) {
								return new BigDecimal(jobj.getString("balance"));
							}
						}
					}
				} catch (IOException e) {
					return new BigDecimal("-1");
				}
			}
		}
		return new BigDecimal("-1");
	}

	public static byte[] generateTransferAssetTransaction(CrptoNetworks nw, byte[] senderPublicKey, String recipient, String assetId, BigDecimal quantity, BigDecimal fee) throws IOException {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				var server_url = opt.get();
				if (!server_url.endsWith("/")) {
					server_url += "/";
				}
				var client = new OkHttpClient.Builder().build();
				var request = new Request.Builder().url(server_url + "burst?requestType=transferAsset")
						.post(RequestBody.create("asset=" + assetId + "&recipient=" + recipient + "&deadline=1440&quantityQNT=" + quantity.toPlainString() + "&broadcast=false&feeNQT="
								+ SignumValue.fromSigna(fee).toNQT() + "&publicKey=" + Hex.toHexString(senderPublicKey), MediaType.parse("application/x-www-form-urlencoded")))
						.build();
				var response = client.newCall(request).execute();
				var jobj = new JSONObject(new JSONTokener(response.body().byteStream()));
				byte[] bArr = Hex.decode(jobj.getString("unsignedTransactionBytes"));
				return bArr;
			}
		}
		throw new UnsupportedOperationException();
	};

	public static byte[] generateTransferAssetTransactionWithMessage(CrptoNetworks nw, byte[] senderPublicKey, String recipient, String assetId, BigDecimal quantity, BigDecimal fee, byte[] message) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				return ns.generateTransferAssetTransactionWithMessage(senderPublicKey, SignumAddress.fromEither(recipient), SignumID.fromLong(assetId),
						SignumValue.fromNQT(new BigInteger(quantity.toPlainString())), SignumValue.ZERO, SignumValue.fromSigna(fee), 1440, message).blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] generateTransferAssetTransactionWithMessage(CrptoNetworks nw, byte[] senderPublicKey, String recipient, String assetId, BigDecimal quantity, BigDecimal fee, String message)
			throws IOException {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				var server_url = opt.get();
				if (!server_url.endsWith("/")) {
					server_url += "/";
				}
				var client = new OkHttpClient.Builder().build();
				var request = new Request.Builder().url(server_url + "burst?requestType=transferAsset")
						.post(RequestBody.create(
								"asset=" + assetId + "&recipient=" + recipient + "&deadline=1440&quantityQNT=" + quantity.toPlainString() + "&broadcast=false&feeNQT="
										+ SignumValue.fromSigna(fee).toNQT() + "&publicKey=" + Hex.toHexString(senderPublicKey) + "&messageIsText=true&message=" + URLEncoder.encode(message, "UTF-8"),
								MediaType.parse("application/x-www-form-urlencoded")))
						.build();
				var response = client.newCall(request).execute();
				var jobj = new JSONObject(new JSONTokener(response.body().byteStream()));
				byte[] bArr = Hex.decode(jobj.getString("unsignedTransactionBytes"));
				return bArr;
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] generateTransaction(CrptoNetworks nw, String recipient, byte[] public_key, BigDecimal amount, BigDecimal fee) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				return ns.generateTransaction(SignumAddress.fromRs(recipient), public_key, SignumValue.fromSigna(amount), SignumValue.fromSigna(fee), 1440, null).blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] generateTransferAssetTransactionWithEncryptedMessage(CrptoNetworks nw, byte[] senderPublicKey, String recipient, String assetId, BigDecimal quantity, BigDecimal fee,
			byte[] message, boolean isText) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				byte[] nounce = new byte[32];// must be 32
				new Random().nextBytes(nounce);
				EncryptedMessage emsg = new EncryptedMessage(message, nounce, isText);
				return ns.generateTransferAssetTransactionWithEncryptedMessage(senderPublicKey, SignumAddress.fromEither(recipient), SignumID.fromLong(assetId),
						SignumValue.fromNQT(new BigInteger(quantity.toPlainString())), SignumValue.ZERO, SignumValue.fromSigna(fee), 1440, emsg).blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] generateTransactionWithEncryptedMessage(CrptoNetworks nw, String recipient, byte[] public_key, BigDecimal amount, BigDecimal fee, byte[] message, boolean isText) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				byte[] nounce = new byte[32];// must be 32
				new Random().nextBytes(nounce);
				EncryptedMessage emsg = new EncryptedMessage(message, nounce, isText);
				return ns.generateTransactionWithEncryptedMessage(SignumAddress.fromRs(recipient), public_key, SignumValue.fromSigna(amount), SignumValue.fromSigna(fee), 1440, emsg, null)
						.blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] generateTransactionWithMessage(CrptoNetworks nw, String recipient, byte[] public_key, BigDecimal amount, BigDecimal fee, String message) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				return ns.generateTransactionWithMessage(SignumAddress.fromRs(recipient), public_key, SignumValue.fromSigna(amount), SignumValue.fromSigna(fee), 1440, message, null).blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] generateTransactionWithMessage(CrptoNetworks nw, String recipient, byte[] public_key, BigDecimal amount, BigDecimal fee, byte[] message) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				return ns.generateTransactionWithMessage(SignumAddress.fromRs(recipient), public_key, SignumValue.fromSigna(amount), SignumValue.fromSigna(fee), 1440, message, null).blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static EncryptedMessage encryptTextMessage(CrptoNetworks nw, byte[] their_public_key, byte[] my_private_key, byte[] message, boolean messageIsText) throws Exception {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			EncryptedMessage e_msg;
			if (messageIsText) {
				e_msg = SignumCrypto.getInstance().encryptTextMessage(new String(message), my_private_key, their_public_key);
			} else {
				e_msg = SignumCrypto.getInstance().encryptBytesMessage(message, my_private_key, their_public_key);
			}
			return e_msg;
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] generateTransactionWithEncryptedMessage(CrptoNetworks nw, String recipient, byte[] senderPublicKey, BigDecimal fee, EncryptedMessage message) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				return ns.generateTransactionWithEncryptedMessage(SignumAddress.fromEither(recipient), senderPublicKey, SignumValue.fromSigna(fee), 1440, message, "").blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] sendMessage(CrptoNetworks nw, String recipient, byte[] public_key, String message) throws IOException {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				var server_url = opt.get();
				if (!server_url.endsWith("/")) {
					server_url += "/";
				}
				var client = new OkHttpClient.Builder().build();
				var request = new Request.Builder().url(server_url + "burst?requestType=sendMessage")
						.post(RequestBody.create("recipient=" + recipient + "&message=" + URLEncoder.encode(message, Charset.defaultCharset())
								+ "&deadline=1440&messageIsText=true&feeNQT=2205000&publicKey=" + Hex.toHexString(public_key), MediaType.parse("application/x-www-form-urlencoded")))
						.build();
				var response = client.newCall(request).execute();
				var jobj = new JSONObject(new JSONTokener(response.body().byteStream()));
				byte[] bArr = Hex.decode(jobj.getString("unsignedTransactionBytes"));
				return bArr;
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] issueAsset(CrptoNetworks nw, String asset_name, String description, long quantityQNT, long feeNQT, byte[] public_key) throws IOException {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			if (feeNQT < 100000000000L) {
				throw new IllegalArgumentException("not enought fee");
			}
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				var server_url = opt.get();
				if (!server_url.endsWith("/")) {
					server_url += "/";
				}
				var client = new OkHttpClient.Builder().build();
				var request = new Request.Builder().url(server_url + "burst?requestType=issueAsset")
						.post(RequestBody.create(
								"name=" + asset_name + "&description=" + description + "&deadline=1440&quantityQNT=" + quantityQNT + "&feeNQT=" + feeNQT + "&publicKey=" + Hex.toHexString(public_key),
								MediaType.parse("application/x-www-form-urlencoded")))
						.build();
				var response = client.newCall(request).execute();
				var jobj = new JSONObject(new JSONTokener(response.body().byteStream()));
				if(jobj.optInt("errorCode",0)!=0) {
					throw new IOException(jobj.optString("errorDescription"));
				}
				byte[] bArr = Hex.decode(jobj.getString("unsignedTransactionBytes"));
				return bArr;
			}
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] signTransaction(CrptoNetworks nw, byte[] privateKey, byte[] unsignedTransaction) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			return SignumCrypto.getInstance().signTransaction(privateKey, unsignedTransaction);
		}
		throw new UnsupportedOperationException();
	}

	public static Object broadcastTransaction(CrptoNetworks nw, byte[] signedTransactionBytes) {
		if (Arrays.asList(SIGNUM, ROTURA).contains(nw)) {
			Optional<String> opt = get_server_url(nw);
			if (opt.isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				return ns.broadcastTransaction(signedTransactionBytes).blockingGet();
			}
		}
		throw new UnsupportedOperationException();
	}

	public static final SignumID[] getSignumTxID(CrptoNetworks nw, String address) throws IllegalArgumentException, InterruptedException, ExecutionException {
		if (address == null || address.isBlank()) {
			return new SignumID[0];
		}
		Optional<String> opt = get_server_url(nw);
		if (opt.isPresent()) {
			NodeService ns = NodeService.getInstance(opt.get());
			SignumID[] id_arr = new SignumID[] {};
			try {
				id_arr = ns.getAccountTransactionIDs(SignumAddress.fromRs(address)).toFuture().get();
			} catch (IllegalArgumentException | InterruptedException | ExecutionException e) {
				if (e.getCause() != null) {
					if (e.getCause().getClass().getName().equals("signumj.entity.response.http.BRSError")) {
						BRSError e1 = (BRSError) e.getCause();
						if (e1.getCode() == 5) {
							return new SignumID[] {};
						}
					}
				}
				throw e;
			}
			return id_arr;
		}
		return new SignumID[] {};
	}

	public static final Transaction getSignumTx(CrptoNetworks nw, SignumID id) throws InterruptedException, ExecutionException {
		Optional<Transaction> o_tx = Optional.empty();
		try {
			o_tx = MyDb.getSignumTxFromLocal(nw, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (o_tx.isPresent()) {
			return o_tx.get();
		} else {
			Optional<String> opt = get_server_url(nw);
			if (get_server_url(nw).isPresent()) {
				NodeService ns = NodeService.getInstance(opt.get());
				Transaction tx = ns.getTransaction(id).toFuture().get();
				if (tx != null) {
					MyDb.putSignumTx(nw, tx);
				}
				return tx;
			}
		}
		throw new InterruptedException();
	}

	public static final Optional<String> get_server_url(CrptoNetworks network) {
		Optional<String> opt = MyDb.get_server_url(network);
		if (opt.isEmpty()) {
			List<String> nws = Arrays.asList();
			try {
				nws = IOUtils.readLines(Util.getResourceAsStream("network/" + network.name().toLowerCase() + ".txt"), "UTF-8");
			} catch (IOException e) {
			}
			if (!nws.isEmpty()) {
				MyDb.update_server_url(network, nws.get(0));
				return Optional.of(nws.get(0));
			}
		}
		return opt;
	}

}
