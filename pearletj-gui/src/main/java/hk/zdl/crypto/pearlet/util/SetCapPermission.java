package hk.zdl.crypto.pearlet.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.commons.io.IOUtils;

public class SetCapPermission {

	public static void main(String[] args) throws Throwable{
		var str = IOUtils.readLines(new ProcessBuilder("which","java").start().getInputStream(),"UTF-8").get(0);
		var path = Paths.get(str);
		while(true) {
			try {
				path = Files.readSymbolicLink(path);
			} catch (IOException e) {
				break;
			}
		}
		var l = new LinkedList<>(Arrays.asList(args));
		l.add(path.toString());
		int i = new ProcessBuilder(l).inheritIO().start().waitFor();
		System.exit(i);
	}

}
