import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

import com.csvreader.CsvWriter;

public class Test {

	public static void main(String[] args) throws Throwable {
		var out = new FileOutputStream(new File("1.csv"));
		var writer = new CsvWriter(out, ',', Charset.forName("UTF-8"));
		writer.writeRecord(new String[] {"a","b","c",","});
		writer.flush();
		writer.close();
	}

}
