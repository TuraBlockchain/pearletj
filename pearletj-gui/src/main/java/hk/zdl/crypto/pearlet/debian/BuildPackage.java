package hk.zdl.crypto.pearlet.debian;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;

import hk.zdl.crypto.pearlet.util.Util;

public class BuildPackage {

	public static void main(String[] args) throws Throwable {
		var authorFullName = Util.getProp().get("authorFullName");
		var appComment = Util.getProp().get("appComment");
		var appNameUp = Util.getProp().get("appName");
		var appName = BuildPackage.class.getPackage().getImplementationTitle();
		var appVer = BuildPackage.class.getPackage().getImplementationVersion();
		var arch = "all";
		var jar_full_name = appName + "-" + appVer + "-jar-with-dependencies.jar";
		var tmp_dir = Files.createTempDirectory("debian");
		Files.createDirectories(tmp_dir.resolve("DEBIAN"));
		Files.createDirectories(tmp_dir.resolve("usr/lib/systemd/system"));
		Files.createDirectories(tmp_dir.resolve("usr/lib/" + appName));
		Files.createDirectories(tmp_dir.resolve("usr/share/" + appName));
		Files.createDirectories(tmp_dir.resolve("usr/share/doc/" + appName));
		Files.createDirectories(tmp_dir.resolve("usr/share/doc"));
		Files.createDirectories(tmp_dir.resolve("usr/share/applications"));
		Files.createDirectories(tmp_dir.resolve("usr/share/swcatalog/yaml"));

		var sb = new StringBuilder();
		sb.append("Source: ").append(appName).append('\n');
		sb.append("Section: net").append('\n');
		sb.append("Priority: optional").append('\n');
		sb.append("Maintainer: ").append(authorFullName).append('\n');
		sb.append("Depends: openjdk-17-jre , libcap2").append('\n');
		sb.append("Version: ").append(appVer).append('\n');
		sb.append("Vcs-Git: ").append("https://gitee.com/nybbs2003/pearletj").append('\n');
		sb.append("Homepage: ").append("http://peth.world").append('\n');
		sb.append("Package: ").append(appName).append('\n');
		sb.append("Architecture: ").append(arch).append('\n');
		sb.append("Description: ").append(appNameUp).append("\n ").append(appComment).append('\n');
		Files.writeString(tmp_dir.resolve("DEBIAN/control"), sb.toString());

		var jar_full_path = "/usr/share/" + appName + "/" + jar_full_name;
		sb = new StringBuilder();
		sb.append("#/usr/bin\n\n");
		sb.append("chmod 755 /usr/share/").append(appName).append("/*.jar\n");
		sb.append("java -cp ").append(jar_full_path).append(" hk.zdl.crypto.pearlet.util.SetCapPermission setcap cap_net_raw,cap_net_admin=eip\n");
		Files.writeString(tmp_dir.resolve("DEBIAN/postinst"), sb.toString());

		sb = new StringBuilder();
		sb.append("#/usr/bin\n\n");
		sb.append("rm -rf /usr/share/" + appName);
		Files.writeString(tmp_dir.resolve("DEBIAN/postrm"), sb.toString());

		sb = new StringBuilder();
		sb.append("Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/").append('\n');
		sb.append("Upstream-Name: ").append(appName).append('\n');
		sb.append("Upstream-Contact: ").append(authorFullName).append('\n');
		sb.append("Source: ").append("https://gitee.com/nybbs2003/pearletj").append('\n');
		sb.append("Files: *").append('\n');
		sb.append("Copyright: ").append(new SimpleDateFormat("yyyy ").format(new Date())).append(authorFullName).append('\n');
		sb.append("License: GPL-2+").append('\n');
		Files.writeString(tmp_dir.resolve("DEBIAN/copyright"), sb.toString());

		sb = new StringBuilder();
		sb.append("[Desktop Entry]\n").append("Name=").append(appNameUp).append('\n');
		sb.append("GenericName=").append(appNameUp).append('\n');
		sb.append("Comment=").append(appComment).append('\n');
		sb.append("Exec=java -jar ").append(jar_full_path).append('\n');
		sb.append("Icon=/usr/share/").append(appName).append("/app_icon.png").append('\n');
		sb.append("StartupWMClass=hk-zdl-crypto-pearlet-Main").append('\n');
		sb.append("Terminal=false").append('\n');
		sb.append("SingleMainWindow=true").append('\n');
		sb.append("Type=Application").append('\n');
		sb.append("Categories=Network;Finance;").append('\n');
		sb.append("Version=").append(appVer).append('\n');
		Files.writeString(tmp_dir.resolve("usr/share/applications/hk.zdl.crypto." + appName + ".desktop"), sb.toString());

		sb = new StringBuilder();
		sb.append(appName).append(" (").append(appVer).append(") trusty; urgency=low\n");
		sb.append("  * Rebuild\n");
		sb.append(" -- ").append(authorFullName).append(' ').append(getServerTime());
		var out = Files.newOutputStream(tmp_dir.resolve("usr/share/doc/" + appName + "/changelog.gz"), StandardOpenOption.CREATE);
		var gzos = new GZIPOutputStream(out);
		gzos.write(sb.toString().getBytes());
		gzos.flush();
		gzos.close();
		out.flush();
		out.close();

		Files.copy(Util.getResourceAsStream("app_icon.png"), tmp_dir.resolve("usr/share/" + appName + "/app_icon.png"));
		Files.copy(Util.getResourceAsStream("lib/libjpcap.so"), tmp_dir.resolve("usr" + "/lib/libjpcap.so"));
		Files.copy(new File(jar_full_name).toPath(), tmp_dir.resolve("usr/share/" + appName + "/" + jar_full_name));

		var package_size = FileUtils.sizeOfDirectory(tmp_dir.toFile());
		Files.writeString(tmp_dir.resolve("DEBIAN/control"), "Installed-Size: " + package_size / 1024 + "\n", StandardOpenOption.APPEND);
		Stream.of("postinst", "postrm").map(s -> tmp_dir.resolve("DEBIAN/" + s).toFile().getAbsolutePath()).forEach(s -> {
			try {
				new ProcessBuilder().command("chmod", "555", s).start();
			} catch (IOException e) {
			}
		});

		new ProcessBuilder().command("chown", "-R", "root", tmp_dir.toFile().getAbsolutePath()).start().waitFor();
		new ProcessBuilder().command("chgrp", "-R", "root,", tmp_dir.toFile().getAbsolutePath()).start().waitFor();
		var proc = new ProcessBuilder().command("dpkg", "--build", tmp_dir.toFile().getAbsolutePath()).start();
		Util.submit(() -> proc.inputReader().lines().forEach(System.out::println));
		Util.submit(() -> proc.errorReader().lines().forEach(System.err::println));

		if (proc.waitFor() == 0) {
			var s = tmp_dir.toFile().getAbsolutePath() + ".deb";
			new ProcessBuilder().command("chmod", "777", s).start();
			new ProcessBuilder().command("rm", "-rf", tmp_dir.toFile().getAbsolutePath()).start();
			Files.move(new File(s).toPath(), new File("./" + appName + "_" + appVer + "_" + arch + ".deb").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} else {
			System.exit(proc.waitFor());
		}
	}

	private static final String getServerTime() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(calendar.getTime());
	}
}
