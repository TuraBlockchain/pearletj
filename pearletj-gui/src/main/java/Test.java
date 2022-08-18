import java.net.URL;

import org.apache.commons.io.IOUtils;

public class Test {

	public static void main(String[] args) throws Throwable {
		System.setProperty("sun.net.spi.nameservice.nameservers", "8.8.8.8");
		System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
		System.getProperties().put("socksProxySet","true"); 
		System.getProperties().put("socksProxyHost","localhost"); 
		System.getProperties().put("socksProxyPort","7070");
		var in = new URL("https://mainnet.infura.io/").openStream();
		IOUtils.copy(in, System.out);
	}

}
