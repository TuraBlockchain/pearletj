package hk.zdl.crypto.pearlet.component.miner.local;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import com.formdev.flatlaf.util.SystemInfo;

public class LocalMiner {

	static {
		try {
			var path = Files.createTempDirectory(null);
			tmp_dir = path.getParent().toFile();
			Files.delete(path);
		} catch (IOException e) {
		}
	}
	private static File tmp_dir;
	private static final String default_console_log_pattern = "{({d(%H:%M:%S)} [{l}]):16.16} {m}{n}";

	public static File build_conf_file(String id, String passphrase, Collection<Path> plot_dirs, URL server_url, String console_log_pattern) throws Exception {
		if (console_log_pattern == null || console_log_pattern.isBlank()) {
			console_log_pattern = default_console_log_pattern;
		}
		var m = new TreeMap<>();
		if (id != null && passphrase != null) {
			m.put("account_id_to_secret_phrase", Collections.singletonMap(new BigInteger(id), passphrase));
		}
		m.put("plot_dirs", plot_dirs.stream().map(o -> o.toAbsolutePath().toString()).toList());
		m.put("url", server_url.toString());
		m.put("cpu_worker_task_count", 1);
		m.put("console_log_pattern", console_log_pattern);
		m.put("logfile_log_pattern", "");
		m.put("logfile_max_count", 0);
		m.put("logfile_max_size", 0);
		if (server_url.getHost().equals("mainnet.ppcoin.cc")) {
			m.put("get_mining_info_interval", 10000);
		}
		m.put("show_progress", false);
		var fz = plot_dirs.parallelStream().flatMap(t -> {
			try {
				return Files.list(t);
			} catch (IOException e) {
				return Stream.empty();
			}
		}).filter(p->p.toFile().getName().startsWith(id+"_")&&Files.isRegularFile(p)).mapToLong(value -> {
			try {
				return Files.size(value);
			} catch (IOException e) {
			return 0;
			}
		}).sum();
		m.put("additional_headers", Collections.singletonMap("X-Filesize", fz));
		File conf_file = File.createTempFile("config-", ".yaml");
		conf_file.deleteOnExit();
		Files.writeString(conf_file.toPath(), new Yaml().dump(m));
		return conf_file;
	}

	public static Process build_process(File miner_bin, File conf_file) throws Exception {
		return new ProcessBuilder(miner_bin.getAbsolutePath(), "-c", conf_file.getAbsolutePath()).directory(tmp_dir).start();
	}

	public static File copy_miner() throws IOException {
		String suffix = "";
		if (SystemInfo.isWindows) {
			suffix = ".exe";
		}
		var tmp_file = File.createTempFile("peth-miner-", suffix);
		tmp_file.deleteOnExit();
		var in_filename = "";
		if (SystemInfo.isLinux) {
			in_filename = "signum-miner";
		} else if (SystemInfo.isWindows) {
			in_filename = "signum-miner.exe";
		} else if (SystemInfo.isMacOS) {
			in_filename = "signum-miner-x86_64-apple-darwin.zip";
		}
		var in = LocalMiner.class.getClassLoader().getResourceAsStream("miner/" + in_filename);
		var out = new FileOutputStream(tmp_file);
		IOUtils.copy(in, out);
		out.flush();
		out.close();
		in.close();
		if (SystemInfo.isMacOS) {
			var zipfile = new ZipFile(tmp_file);
			var entry = zipfile.stream().findAny().get();
			in = zipfile.getInputStream(entry);
			tmp_file = File.createTempFile("peth-miner-", ".app");
			tmp_file.deleteOnExit();
			out = new FileOutputStream(tmp_file);
			IOUtils.copy(in, out);
			out.flush();
			out.close();
			in.close();
			zipfile.close();
		}
		tmp_file.setExecutable(true);
		return tmp_file;
	}
}
