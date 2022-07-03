package hk.zdl.crpto.pearlet.util;

import java.io.File;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class Util {

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

}
