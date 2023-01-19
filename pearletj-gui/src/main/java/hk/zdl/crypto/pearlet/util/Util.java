package hk.zdl.crypto.pearlet.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

import hk.zdl.crypto.pearlet.ds.RoturaAddress;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import signumj.entity.response.Transaction;

public class Util {

	private static final ExecutorService es = Executors.newCachedThreadPool((r) -> {
		Thread t = new Thread(r, "");
		t.setDaemon(true);
		return t;
	});

	public static final Map<String, String> default_currency_symbol = Collections
			.unmodifiableMap(Stream.of(new String[] { "SIGNUM", String.valueOf((char) 0xA7A8) }, new String[] { "ROTURA", "PETH" }, new String[] { "WEB3J", "ETH" })
					.map(s -> Collections.singletonMap(s[0], s[1])).reduce(new TreeMap<>(), (x, o) -> {
						x.putAll(o);
						return x;
					}));

	public static final Prop getProp() {
		return PropKit.use("config.txt");
	}

	public static final String getDBURL() {
		Prop prop = getProp();
		return getDBURL(prop.get("appName"), prop.get("appVersion"), prop.get("appAuthor"), prop.get("dbName"));
	}

	public static final String getDBURL(String app_name, String app_version, String author, String db_name) {
		AppDirs appDirs = AppDirsFactory.getInstance();
		String user_dir = appDirs.getUserDataDir(app_name, app_version, author, false);
		user_dir += File.separator + db_name;
		user_dir = user_dir.replace('\\', '/');
		String db_url = "jdbc:derby:directory:" + user_dir + ";create=true";
		return db_url;
	}

	private static final Properties user_settings = new Properties();

	public static final Properties getUserSettings() {
		if (user_settings.isEmpty()) {
			try {
				loadUserSettings();
			} catch (IOException e) {
			}
		}
		return user_settings;
	}

	public static final void loadUserSettings() throws IOException {
		Prop prop = getProp();
		AppDirs appDirs = AppDirsFactory.getInstance();
		String user_dir = appDirs.getUserDataDir(prop.get("appName"), prop.get("appVersion"), prop.get("appAuthor"), false);
		user_dir += File.separator + "settings.txt";
		var path = Paths.get(user_dir);
		if (Files.exists(path)) {
			user_settings.load(Files.newInputStream(path));
		}
	}

	public static final void saveUserSettings() throws IOException {
		AppDirs appDirs = AppDirsFactory.getInstance();
		Prop prop = getProp();
		String user_dir = appDirs.getUserDataDir(prop.get("appName"), prop.get("appVersion"), prop.get("appAuthor"), false);
		user_dir += File.separator + "settings.txt";
		user_settings.store(Files.newOutputStream(Paths.get(user_dir), StandardOpenOption.CREATE), null);
	}

	public static final String getAppVersion() {
		var text = Util.class.getPackage().getImplementationVersion();
		if (text == null) {
			text = getProp().get("appVersion");
		}
		return text;
	}

	public static final String getResourceAsText(String path) {
		try {
			return IOUtils.toString(Util.class.getClassLoader().getResourceAsStream(path), Charset.defaultCharset());
		} catch (IOException e) {
			return null;
		}
	}

	public static final byte[] getResourceAsByteArray(String path) {
		try {
			return IOUtils.toByteArray(Util.class.getClassLoader().getResourceAsStream(path));
		} catch (IOException e) {
			return null;
		}
	}

	public static final InputStream getResourceAsStream(String path) {
		return Util.class.getClassLoader().getResourceAsStream(path);
	}

	public static final URL getResource(String path) {
		return Util.class.getClassLoader().getResource(path);
	}

	public static final <T> Future<T> submit(Callable<T> task) {
		return es.submit(task);
	}

	public static final Future<?> submit(Runnable task) {
		return es.submit(task);
	}

	public static Long getTime(Class<?> cl) {
		try {
			String rn = cl.getName().replace('.', '/') + ".class";
			JarURLConnection j = (JarURLConnection) cl.getClassLoader().getResource(rn).openConnection();
			return j.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
		} catch (Exception e) {
			return null;
		}
	}

	public static final <E> boolean viewContractDetail(CrptoNetworks nw, E e) {
		if (!Desktop.isDesktopSupported()) {
			return false;
		}
		switch (nw) {
		case ROTURA:
			break;
		case SIGNUM:
			break;
		case WEB3J:
			try {
				var address = ((JSONObject) e).getString("contract_address");
				if ("0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee".equals(address)) {
					return false;
				}
				Desktop.getDesktop().browse(new URI("https://ethplorer.io/address/" + address + "#pageTab=issuances&tab=tab-issuances"));
			} catch (Exception x) {
				return false;
			}
			break;
		default:
			break;

		}
		return false;
	}

	public static final <E> boolean viewTxDetail(CrptoNetworks nw, E e) {
		if (!Desktop.isDesktopSupported()) {
			return false;
		}
		switch (nw) {
		case ROTURA:
			try {
				Transaction tx = (Transaction) e;
				String tx_id = tx.getId().toString();
				Desktop.getDesktop().browse(new URI("http://explorer.peth.world:8000/tx/" + tx_id));
			} catch (Exception x) {
				return false;
			}
			break;
		case SIGNUM:
			try {
				Transaction tx = (Transaction) e;
				String tx_id = tx.getId().toString();
				Desktop.getDesktop().browse(new URI("https://chain.signum.network/tx/" + tx_id));
			} catch (Exception x) {
				return false;
			}
			break;
		case WEB3J:
			JSONObject tx = (JSONObject) e;
			try {
				Desktop.getDesktop().browse(new URI("https://www.blockchain.com/eth/tx/" + tx.getString("tx_hash")));
			} catch (Exception x) {
				return false;
			}
			break;
		default:
			break;

		}
		return false;
	}

	public static final boolean viewAccountDetail(CrptoNetworks nw, String e) {
		if (!Desktop.isDesktopSupported()) {
			return false;
		}
		switch (nw) {
		case ROTURA:
			try {
				RoturaAddress adr = RoturaAddress.fromEither(e);
				Desktop.getDesktop().browse(new URI("http://explorer.peth.world:8000/address/" + adr.getID()));
			} catch (Exception x) {
				return false;
			}
			break;
		case SIGNUM:
			try {
				Desktop.getDesktop().browse(new URI("https://chain.signum.network/search/?q=" + e));
			} catch (Exception x) {
				return false;
			}
			break;
		case WEB3J:
			try {
				Desktop.getDesktop().browse(new URI("https://www.blockchain.com/eth/address/" + e));
			} catch (Exception x) {
				return false;
			}
			break;
		default:
			break;

		}
		return false;
	}

}
