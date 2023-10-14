package hk.zdl.crypto.pearlet.plot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import com.formdev.flatlaf.util.SystemInfo;

public class PlotUtil {

	public static void do_plot(Path dir, String id, long nounce, PlotProgressListener listener, String... mem_usage) throws Exception {
		var plotter_bin_path = copy_plotter().toPath();
		var proc = PlotUtil.plot(plotter_bin_path, dir, false, new BigInteger(id), Math.abs(new Random().nextInt()), nounce, listener, mem_usage);
		int i = proc.waitFor();
		if (i != 0) {
			var err_info = IOUtils.readLines(proc.getErrorStream(), Charset.defaultCharset()).stream().reduce("", (a, b) -> a + "\n" + b).trim();
			throw new IOException(err_info);
		}
		Files.deleteIfExists(plotter_bin_path);
	}

	private static File copy_plotter() throws IOException {
		String suffix = "";
		if (SystemInfo.isWindows) {
			suffix = ".exe";
		}
		var tmp_file = File.createTempFile("tura-plotter-", suffix);
		tmp_file.deleteOnExit();
		String in_filename = "";
		if (SystemInfo.isLinux || SystemInfo.isAARCH64) {
			in_filename = "signum-plotter";
		} else if (SystemInfo.isWindows) {
			in_filename = "signum-plotter.exe";
		} else if (SystemInfo.isMacOS) {
			in_filename = "signum-plotter-x86_64-apple-darwin.zip";
		}
		var in = PlotUtil.class.getClassLoader().getResourceAsStream("plotter/" + in_filename);
		var out = new FileOutputStream(tmp_file);
		IOUtils.copy(in, out);
		out.flush();
		out.close();
		in.close();
		if (in_filename.endsWith(".zip")) {
			var zipfile = new ZipFile(tmp_file);
			var entry = zipfile.stream().findAny().get();
			in = zipfile.getInputStream(entry);
			tmp_file = File.createTempFile("tura-plotter-", ".app");
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

	public static final Process plot(Path plot_bin, Path target, boolean benchmark, BigInteger id, long start_nonce, long nonces, PlotProgressListener listener, String... mem_usage) throws Exception {
		if (mem_usage == null || mem_usage.length < 1) {
			mem_usage = new String[] { "1GiB" };
		}
		if (!Files.exists(plot_bin.toAbsolutePath())) {
			plot_bin = findPath(plot_bin);
		}
		if (!plot_bin.toFile().exists()) {
			throw new FileNotFoundException(plot_bin.toString());
		} else if (!plot_bin.toFile().isFile()) {
			throw new FileNotFoundException("not a file: " + plot_bin.toString());
		} else if (!plot_bin.toFile().canRead()) {
			throw new IOException("cannot read: " + plot_bin.toString());
		} else if (!plot_bin.toFile().canExecute()) {
			throw new IOException("not executable: " + plot_bin.toString());
		}
		if (!target.toFile().exists()) {
			throw new FileNotFoundException(target.toString());
		} else if (!target.toFile().isDirectory()) {
			throw new IOException("not dir: " + target.toString());
		}
		var l = new LinkedList<String>();
		if (SystemInfo.isAARCH64) {
			var proc = new ProcessBuilder("docker", "run", "--privileged", "--rm", "tonistiigi/binfmt", "--install", "linux/amd64").start();
			int i = proc.waitFor();
			if (i != 0) {
				var err_info = IOUtils.readLines(proc.getErrorStream(), Charset.defaultCharset()).stream().reduce("", (a, b) -> a + "\n" + b).trim();
				throw new IOException(err_info);
			}
			l.addAll(Arrays.asList("docker", "run", "--platform", "linux/amd64", "--mount", "type=bind,source=" + plot_bin.toAbsolutePath().toString() + ",target=/app/signum-plotter", "--mount",
					"type=bind,source=" + target.toAbsolutePath().toString() + ",target=" + target.toAbsolutePath().toString(), "ubuntu", "/app/signum-plotter"));
		} else {
			l.add(plot_bin.toAbsolutePath().toString());
		}
		if (benchmark) {
			l.add("-b");
		}
		l.addAll(Arrays.asList("--id", id.toString(), "--sn", Long.toString(start_nonce), "--n", Long.toString(nonces), "-m", mem_usage[0], "-p", target.toAbsolutePath().toString()));
		var proc = new ProcessBuilder(l).start();
		var reader = proc.inputReader(Charset.defaultCharset());
		String line = null;
		while (true) {
			line = reader.readLine();
			if (line == null) {
				break;
			} else {
				line = line.trim();
			}
			if (line.isEmpty() || line.equals("[2A")) {
				continue;
			} else if (line.startsWith("Error: ")) {
				reader.close();
				throw new IOException(line.substring("Error: ".length()));
			} else if (line.equals("Starting plotting...")) {
				continue;
			} else {
				if (line.isEmpty() || line.equals("[2A")) {
					continue;
				} else {
					if (line.contains("鈹傗")) {
						byte[] bArr = line.getBytes("GBK");
						line = new String(bArr, "UTF-8");
						line = line.replace("�?", "│");
					}
				}
				if (line.startsWith("Hashing:") || line.startsWith("Writing:")) {
					PlotProgressListener.Type type = line.startsWith("H") ? PlotProgressListener.Type.HASH : PlotProgressListener.Type.WRIT;
					line = line.substring(line.lastIndexOf('│') + 1);
					float progress = Float.parseFloat(line.substring(0, line.indexOf('%')).trim());
					line = line.substring(line.indexOf('%') + 1).trim();
					String rate, eta = "";
					if (line.endsWith("B/s")) {
						rate = line;
					} else {
						rate = line.substring(0, line.lastIndexOf(" ")).trim().replace(" ", "");
						eta = line.substring(line.lastIndexOf(" ")).trim();
					}
					listener.onProgress(type, progress, rate, eta);
				}
			}

		}
		return proc;
	}

	private static final Path findPath(Path p) throws IOException {
		return IOUtils.readLines(new ProcessBuilder().command("which", p.toString()).start().getInputStream(), "UTF-8").stream().map(Paths::get).findFirst().get();
	}

}
