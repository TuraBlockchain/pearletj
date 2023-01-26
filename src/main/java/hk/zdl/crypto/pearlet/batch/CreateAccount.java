package hk.zdl.crypto.pearlet.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;

import com.csvreader.CsvWriter;

import hk.zdl.crypto.pearlet.component.account_settings.signum.CreateSignumAccount;
import hk.zdl.crypto.pearlet.ds.RoturaAddress;

public class CreateAccount {

	private static final List<String> mnemoic = new BufferedReader(new InputStreamReader(CreateSignumAccount.class.getClassLoader().getResourceAsStream("en-mnemonic-word-list.txt"))).lines()
			.filter(s -> !s.isEmpty()).toList();
	public static void main(String[] args) throws Throwable {
		var count = 100;
		var fn = "account.csv";
		if (args.length >= 1) {
			count = Integer.parseInt(args[0]);
		}
		if (args.length >= 2) {
			fn = args[1];
		}
		var out = new FileOutputStream(new File(fn));
		var writer = new CsvWriter(out, ',', Charset.defaultCharset());
		writer.writeRecord(new String[] { "id", "address", "phrase" });
		var rand = new Random();
		for (var i = 0; i < count; i++) {
			var sb = new StringBuilder();
			for (var j = 0; j < 12; j++) {
				sb.append(mnemoic.get(rand.nextInt(mnemoic.size())));
				sb.append(' ');
			}
			var phrase = sb.toString().trim();
			var r_adr = RoturaAddress.fromPassPhrase(phrase);
			var id = r_adr.getID();
			var adr = r_adr.getFullAddress();
			writer.writeRecord(new String[] {id,adr,phrase});
		}
		writer.flush();
		writer.close();
		out.flush();
		out.close();
	}

}
