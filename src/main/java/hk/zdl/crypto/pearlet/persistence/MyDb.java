package hk.zdl.crypto.pearlet.persistence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.json.JSONArray;
import org.json.JSONTokener;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.dialect.AnsiSqlDialect;
import com.jfinal.plugin.c3p0.C3p0Plugin;

import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.lock.WalletLock;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.entity.SignumID;
import signumj.entity.response.Transaction;

public class MyDb {

	static {
		String db_url = Util.getDBURL();
		C3p0Plugin dp = new C3p0Plugin(db_url, "", "");
		ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
		arp.setDialect(new AnsiSqlDialect());
		dp.start();
		arp.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				arp.stop();
				dp.stop();
			}
		});
	}

	public static final List<String> getTables() {
		return Db.query("select st.tablename from sys.systables st LEFT OUTER join sys.sysschemas ss on (st.schemaid = ss.schemaid) where ss.schemaname ='APP'");
	}

	public static final boolean create_table(String table_name) {
		Prop prop = PropKit.use("sql/create_tables.txt");
		String sql = prop.get(table_name);
		if (sql == null || sql.isBlank()) {
			return false;
		}
		try {
			var conn = Db.use().getConfig().getConnection();
			var st = conn.createStatement();
			st.execute(sql);
			st.getResultSet();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	public static final void create_missing_tables() {
		List<String> tables = getTables();
		Prop prop = PropKit.use("sql/create_tables.txt");
		prop.getProperties().keySet().stream().map(o -> o.toString().trim().toUpperCase()).filter(s -> !tables.contains(s)).map(s -> s.toLowerCase()).forEach(MyDb::create_table);
	}

	public static final List<CryptoNetwork> get_networks() {
		return Db.find("SELECT * FROM NETWORKS").stream().map(r -> {
			var n = new CryptoNetwork();
			n.setId(r.getInt("ID"));
			n.setType(CryptoNetwork.Type.valueOf(r.getStr("NETWORK")));
			n.setName(r.getStr("NWNAME"));
			n.setUrl(r.getStr("URL"));
			return n;
		}).toList();
	}

	public static final boolean insert_network(CryptoNetwork nw) {
		return Db.save("NETWORKS", new Record().set("NETWORK", nw.getType().name()).set("NWNAME", nw.getName()).set("URL", nw.getUrl()));
	}

	public static final boolean update_network(CryptoNetwork nw) {
		if (nw.getId() < 1) {
			return false;
		}
		var r = Db.findFirst("select * from networks where id = ?", nw.getId());
		r.set("NWNAME", nw.getName()).set("URL", nw.getUrl());
		return Db.update("NETWORKS", "ID", r);
	}

	public static final boolean delete_network(int id) {
		Db.delete("DELETE FROM SIGNUM_TX WHERE NWID=?", id);
		Db.delete("DELETE FROM ACCOUNTS WHERE NWID=?", id);
		Db.delete("DELETE FROM ENCPVK WHERE NWID=?", id);
		Db.delete("DELETE FROM ENCPSE WHERE NWID=?", id);
		return Db.deleteById("NETWORKS", "ID", id);
	}

	public static final Optional<String> get_server_url(CryptoNetwork network) {
		List<Record> l = Db.find("SELECT * FROM NETWORKS WHERE ID = ?", network.getId());
		if (l.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(l.get(0).getStr("URL"));
		}
	}

	public static final Optional<Record> get_webj_auth() {
		List<Record> l = Db.find("select * from WEBJAUTH");
		if (l.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(l.get(0));
		}
	}

	public static final boolean update_webj_auth(String auth_id, String scret) {
		if (scret.equals("unchanged")) {
			return true;
		}
		List<Record> l = Db.find("SELECT * FROM WEBJAUTH");
		if (l.isEmpty()) {
			var o = new Record().set("MYAUTH", auth_id).set("SECRET", scret);
			return Db.save("WEBJAUTH", o);
		} else {
			var o = l.get(0);
			o.set("MYAUTH", auth_id).set("SECRET", scret);
			return Db.update("WEBJAUTH", "ID", o);
		}
	}

	public static final Optional<Boolean> isWatchAccount(CryptoNetwork network, String address) {
		Record r = Db.findFirst("SELECT * FROM ACCOUNTS WHERE NWID = ? AND ADDRESS = ?", network.getId(), address);
		if (r == null) {
			return Optional.empty();
		} else {
			byte[] arr = r.getBytes("PRIVATE_KEY");
			return Optional.of(arr == null || arr.length < 1);
		}
	}

	public static final List<Record> getAccounts(CryptoNetwork network) {
		return Db.find("SELECT * FROM ACCOUNTS WHERE NWID = ?", network.getId());
	}

	public static final Optional<Record> getAccount(CryptoNetwork network, String address) {
		if (network == null || address == null) {
			return Optional.empty();
		}
		Record r = Db.findFirst("SELECT * FROM ACCOUNTS WHERE NWID = ? AND ADDRESS = ?", network.getId(), address);
		return r == null ? Optional.empty() : Optional.of(r);
	}

	public static final List<Record> getAccounts() {
		return Db.find("SELECT * FROM ACCOUNTS");
	}

	public static final boolean insertAccount(CryptoNetwork nw, String address, byte[] public_key, byte[] private_key) {
		int i = Db.queryInt("SELECT COUNT(*) FROM ACCOUNTS WHERE NWID = ? AND ADDRESS = ?", nw.getId(), address);
		if (i > 0) {
			return false;
		} else if (WalletLock.hasPassword()) {
			if (WalletLock.isLocked()) {
				var o = WalletLock.unlock();
				if (!o.isPresent() || o.get() == false) {
					throw new IllegalStateException("Failed to unlock wallet!");
				}
			}
			try {
				private_key = WalletLock.encrypt_private_key(private_key);
			} catch (Exception x) {
				return false;
			}
		}
		var o = new Record().set("NWID", nw.getId()).set("NETWORK", nw.getType().name()).set("ADDRESS", address).set("PUBLIC_KEY", public_key).set("PRIVATE_KEY", private_key);
		return Db.save("ACCOUNTS", "ID", o);
	}

	public static final int[] batch_mark_account_with_encpvk(List<? extends Record> recordList) {
		for (var r : recordList) {
			r.set("PRIVATE_KEY", new byte[] { 1 });
		}
		return Db.batchUpdate("ACCOUNTS", recordList, 10);
	}

	public static final boolean mark_account_with_encpvk(Record r) {
		r.set("PRIVATE_KEY", new byte[] { 1 });
		return Db.update("ACCOUNTS", r);
	}

	public static final boolean deleteAccount(int id) {
		int nwid = Db.queryInt("SELECT NWID FROM ACCOUNTS WHERE ID = ? ", id);
		delete_encpvk(nwid, id);
		delete_encpse(nwid, id);
		return Db.deleteById("ACCOUNTS", "ID", id);
	}

	public static final boolean putSignumTx(CryptoNetwork nw, Transaction tx) {
		if (!(tx instanceof java.io.Serializable)) {
			return false;
		}
		long id = tx.getId().getSignedLongId();
		var baos = new ByteArrayOutputStream(10240);
		try {
			var oos = new ObjectOutputStream(baos);
			oos.writeObject(tx);
			oos.close();
		} catch (IOException e) {
			Logger.getLogger(MyDb.class.getName()).log(Level.WARNING, e.getMessage(), e);
		}
		byte[] bArr = baos.toByteArray();
		return Db.save("SIGNUM_TX", "ID", new Record().set("ID", id).set("NWID", nw.getId()).set("CONTENT", bArr));
	}

	public static final Optional<Transaction> getSignumTxFromLocal(CryptoNetwork nw, SignumID id) throws Exception {
		if (!(id instanceof java.io.Serializable)) {
			return Optional.empty();
		}
		Connection conn = Db.use().getConfig().getConnection();
		PreparedStatement pst = conn.prepareStatement("SELECT CONTENT FROM APP.SIGNUM_TX WHERE NWID = ? AND ID = ?");
		Db.use().getConfig().getDialect().fillStatement(pst, nw.getId(), id.getSignedLongId());
		ResultSet rs = pst.executeQuery();
		if (rs.next()) {
			InputStream in = rs.getBinaryStream(1);
			ObjectInputStream ois = new ObjectInputStream(in);
			Transaction tx = (Transaction) ois.readObject();
			ois.close();
			in.close();
			rs.close();
			conn.close();
			Optional<Transaction> o_tx = Optional.of(tx);
			return o_tx;
		} else {
			return Optional.empty();
		}
	}

	public static final boolean putETHTokenList(String address, JSONArray jarr) {
		try {
			boolean save = false;
			Record r = Db.findFirst("SELECT * FROM APP.ETH_TOKENS WHERE ADDRESS = ?", address);
			if (r == null) {
				r = new Record().set("ADDRESS", address);
				save = true;
			}
			var baos = new ByteArrayOutputStream(10240);
			var gzos = new GZIPOutputStream(baos, true);
			gzos.write(jarr.toString().getBytes());
			gzos.flush();
			gzos.finish();
			byte[] bArr = baos.toByteArray();
			r.set("CONTENT", bArr);
			if (save) {
				return Db.save("APP.ETH_TOKENS", "ID", r);
			} else {
				return Db.update("APP.ETH_TOKENS", "ID", r);
			}
		} catch (Exception e) {
			Logger.getLogger(MyDb.class.getName()).log(Level.WARNING, e.getMessage(), e);
		}
		return false;
	}

	public static final Optional<JSONArray> getETHTokenList(String address) throws Exception {
		Connection conn = Db.use().getConfig().getConnection();
		PreparedStatement pst = conn.prepareStatement("SELECT CONTENT FROM APP.ETH_TOKENS WHERE ADDRESS = ?");
		Db.use().getConfig().getDialect().fillStatement(pst, address);
		ResultSet rs = pst.executeQuery();
		if (rs.next()) {
			InputStream in = rs.getBinaryStream(1);
			GZIPInputStream gis = new GZIPInputStream(in);
			JSONArray jarr = new JSONArray(new JSONTokener(gis));
			gis.close();
			in.close();
			rs.close();
			conn.close();
			Optional<JSONArray> o_tx = Optional.of(jarr);
			return o_tx;
		} else {
			return Optional.empty();
		}
	}

	public static final List<Path> getMinerPaths(CryptoNetwork nw, String id) {
		return Db.find("SELECT PATH FROM APP.MPATH WHERE NETWORK = ? AND ACCOUNT = ?", nw.getType().name(), id).stream().map(o -> Paths.get(o.getStr("PATH"))).toList();
	}

	public static final boolean addMinerPath(CryptoNetwork nw, String id, Path path) {
		int i = Db.queryInt("SELECT COUNT(*) FROM APP.MPATH WHERE NETWORK = ? AND ACCOUNT = ? AND PATH = ?", nw.getType().name(), id, path.toAbsolutePath().toString());
		if (i > 0) {
			return false;
		}
		var o = new Record().set("NETWORK", nw.getType().name()).set("ACCOUNT", id).set("PATH", path.toAbsolutePath().toString());
		return Db.save("MPATH", "ID", o);
	}

	public static final boolean delMinerPath(CryptoNetwork nw, String id, Path path) {
		var r = Db.findFirst("SELECT * FROM APP.MPATH WHERE NETWORK = ? AND ACCOUNT = ? AND PATH = ?", nw.getType().name(), id, path.toAbsolutePath().toString());
		if (r != null) {
			return Db.deleteById("MPATH", "ID", r.get("ID"));
		} else {
			return false;
		}
	}

	public static final List<Record> find_all_encpvk() {
		return Db.findAll("ENCPVK");
	}

	public static final int[] batch_update_encpvk(List<? extends Record> recordList) {
		return Db.batchUpdate("ENCPVK", recordList, 10);
	}

	public static final synchronized boolean insert_or_update_encpvk(int network_id, int account_id, byte[] content) {
		var r = Db.findFirst("SELECT * FROM APP.ENCPVK WHERE NWID = ? AND ACID = ?", network_id, account_id);
		if (r == null) {
			var o = new Record().set("NWID", network_id).set("ACID", account_id).set("CONTENT", content);
			return Db.save("ENCPVK", "ID", o);
		} else {
			r.set("CONTENT", content);
			return Db.update("ENCPVK", r);
		}
	}

	public static final byte[] get_encpvk(int network_id, int account_id) {
		return Db.findFirst("SELECT CONTENT FROM APP.ENCPVK WHERE NWID = ? AND ACID = ?", network_id, account_id).getBytes("CONTENT");
	}

	public static final int delete_encpvk(int network_id, int account_id) {
		return Db.delete("DELETE FROM APP.ENCPVK WHERE NWID = ? AND ACID = ?", network_id, account_id);
	}

	public static final List<Record> find_all_encpse() {
		return Db.findAll("ENCPSE");
	}

	public static final int[] batch_update_encpse(List<? extends Record> recordList) {
		return Db.batchUpdate("ENCPSE", recordList, 10);
	}

	public static final synchronized boolean insert_or_update_encpse(int network_id, int account_id, byte[] content) {
		var r = Db.findFirst("SELECT * FROM APP.ENCPSE WHERE NWID = ? AND ACID = ?", network_id, account_id);
		if (r == null) {
			var o = new Record().set("NWID", network_id).set("ACID", account_id).set("CONTENT", content);
			return Db.save("ENCPSE", "ID", o);
		} else {
			r.set("CONTENT", content);
			return Db.update("ENCPSE", r);
		}
	}

	public static final byte[] get_encpse(int network_id, int account_id) {
		return Db.findFirst("SELECT CONTENT FROM APP.ENCPSE WHERE NWID = ? AND ACID = ?", network_id, account_id).getBytes("CONTENT");
	}

	public static final int delete_encpse(int network_id, int account_id) {
		return Db.delete("DELETE FROM APP.ENCPSE WHERE NWID = ? AND ACID = ?", network_id, account_id);
	}
}
