import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;

import hk.zdl.crypto.pearlet.component.miner.local.LocalMiner;

public class Test {

	public static void main(String[] args) throws Throwable {
		var proc = LocalMiner.start("10282355196851764065", "glad suffer red during single glow shut slam hill death lust although", Arrays.asList(Paths.get("/Users/david/")),
				new URL("http://mainnet.peth.world:6876"), null);
		var r = proc.inputReader();
		while (true) {
			var line = r.readLine().trim();
			if(line.isBlank()) {
				continue;
			}
			System.out.println(line);
		}
	}

}
