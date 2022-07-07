package hk.zdl.crpto.pearlet.persistence;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.dialect.AnsiSqlDialect;
import com.jfinal.plugin.c3p0.C3p0Plugin;

import hk.zdl.crpto.pearlet.util.CrptoNetworks;
import hk.zdl.crpto.pearlet.util.Util;

public class MyDb {

	static {
		String db_url = Util.getDBURL();
		C3p0Plugin dp = new C3p0Plugin(db_url, "", "");
		ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
		arp.setDialect(new AnsiSqlDialect());
		dp.start();
		arp.start();
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

	public static final Optional<String> get_server_url(CrptoNetworks network) {
		List<Record> l = Db.find("select * from networks where network = ?", network.name());
		if (l.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(l.get(0).getStr("URL"));
		}
	}

	public static final boolean update_server_url(CrptoNetworks network, String url) {
		List<Record> l = Db.find("select * from networks where network = ?", network.name());
		if (l.isEmpty()) {
			var o = new Record().set("network", network.name()).set("URL", url);
			return Db.save("networks", o);
		} else {
			var o = l.get(0);
			o.set("URL", url);
			return Db.update("networks", "ID", o);
		}
	}

	public static final Optional<Record> get_webj_auth() {
		List<Record> l = Db.find("select MYAUTH from WEBJAUTH");
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
		List<Record> l = Db.find("select * from WEBJAUTH");
		if (l.isEmpty()) {
			var o = new Record().set("MYAUTH", auth_id).set("SECRET", scret);
			return Db.save("WEBJAUTH", o);
		} else {
			var o = l.get(0);
			o.set("MYAUTH", auth_id).set("SECRET", scret);
			return Db.update("WEBJAUTH", "ID", o);
		}
	}

	public static final List<Record> getAccounts(CrptoNetworks network) {
		return Db.find("select * from ACCOUNTS WHERE NETWORK = ?", network.name());
	}

	public static final List<Record> getAccounts() {
		return Db.find("select * from ACCOUNTS");
	}

	public static final boolean insertAccount(CrptoNetworks network, byte[] public_key, byte[] private_key) {
		var o = new Record().set("NETWORK", network.name()).set("PUBLIC_KEY", public_key).set("PRIVATE_KEY", private_key);
		return Db.save("ACCOUNTS", "ID", o);
	}

	public static final boolean deleteAccount(int id) {
		return Db.deleteById("ACCOUNTS", "ID", id);
	}

}
