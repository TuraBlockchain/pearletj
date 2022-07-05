package hk.zdl.crpto.pearlet.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class Util {

	private static final ExecutorService es = Executors.newCachedThreadPool((r) -> {
		Thread t = new Thread(r, "");
		t.setDaemon(true);
		return t;
	});

	public static final Map<String, String> default_currency_symbol = Collections
			.unmodifiableMap(Stream.of(new String[] { "SIGNUM", String.valueOf((char) 0xA7A8) }, new String[] { "ROTURA", "XRT" }, new String[] { "WEB3J", "ETH" })
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

	public static final <T> Future<T> submit(Callable<T> task) {
		return es.submit(task);
	}

	public static final Future<?> submit(Runnable task) {
		return es.submit(task);
	}

}
