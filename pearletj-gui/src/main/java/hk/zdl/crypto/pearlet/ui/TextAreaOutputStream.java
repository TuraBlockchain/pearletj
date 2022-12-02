package hk.zdl.crypto.pearlet.ui;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.List;
import javax.swing.*;

public class TextAreaOutputStream extends Writer {

// *************************************************************************************************
// INSTANCE PROPERTIES
// *************************************************************************************************

	private Appender appender; // most recent action

// *************************************************************************************************
// INSTANCE CONSTRUCTORS/INIT/CLOSE/FINALIZE
// *************************************************************************************************

	public TextAreaOutputStream(JTextArea txtara) {
		this(txtara, 1000);
	}

	public TextAreaOutputStream(JTextArea txtara, int maxlin) {
		this(txtara, maxlin, null);
	}

	public TextAreaOutputStream(JTextArea txtara, int maxlin, Pattern rmvptn) {
		if (maxlin < 1) {
			throw new IllegalArgumentException("TextAreaOutputStream maximum lines must be positive (value=" + maxlin + ")");
		}
		appender = new Appender(txtara, maxlin, rmvptn);
	}

// *************************************************************************************************
// INSTANCE METHODS - ACCESSORS
// *************************************************************************************************

	/** Clear the current console text area. */
	public synchronized void clear() {
		if (appender != null) {
			appender.clear();
		}
	}

// *************************************************************************************************
// INSTANCE METHODS - OUTPUT STREAM IMPLEMENTATION
// *************************************************************************************************

	public synchronized void close() {
		appender = null;
	}

	public synchronized void flush() {
	}

	public synchronized void write(byte[] ba) {
		write(ba, 0, ba.length);
	}

	public synchronized void write(byte[] ba, int str, int len) {
		if (appender != null) {
			appender.append(bytesToString(ba, str, len));
		}
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		appender.append(new String(cbuf, off, len));
	}

	@Override
	public void write(String str) throws IOException {
		appender.append(str);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		appender.append(str.substring(off, off + len));
	}

// *************************************************************************************************
// INSTANCE METHODS - UTILITY
// *************************************************************************************************

	static private String bytesToString(byte[] ba, int str, int len) {
		try {
			return new String(ba, str, len, "UTF-8");
		} catch (UnsupportedEncodingException thr) {
			return new String(ba, str, len);
		} // all JVMs are required to support UTF-8
	}

// *************************************************************************************************
// STATIC NESTED CLASSES
// *************************************************************************************************

	static class Appender implements Runnable {
		private final StringBuilder line = new StringBuilder(1000); // current line being assembled
		private final List<String> lines = new ArrayList<String>(); // lines waiting to be appended
		private final LinkedList<Integer> lengths = new LinkedList<Integer>(); // lengths of each line within text area

		private final JTextArea textArea;
		private final int maxLines; // maximum lines allowed in text area
		private final Pattern rmvPattern;

		private boolean clear;
		private boolean queue;

		Appender(JTextArea txtara, int maxlin, Pattern rmvptn) {
			textArea = txtara;
			maxLines = maxlin;
			rmvPattern = rmvptn;

			clear = false;
			queue = true;
		}

		synchronized void append(String val) {
			boolean eol = val.endsWith(EOL1) || val.endsWith(EOL2);

			line.append(val);
			while (line.length() > LINE_MAX) {
				emitLine(line.substring(0, LINE_MAX) + EOL1);
				line.replace(0, LINE_MAX, "[>>] ");
			}
			if (eol) {
				emitLine(line.toString());
				line.setLength(0);
			}
		}

		private void emitLine(String lin) {
			if (lines.size() > 10_000) {
				lines.clear();
				lines.add("<console-overflowed>\n");
			} else {
				if (rmvPattern != null) {
					lin = rmvPattern.matcher(lin).replaceAll("");
				}
				lines.add(lin);
			}
			if (queue) {
				queue = false;
				EventQueue.invokeLater(this);
			}
		}

		synchronized void clear() {
			clear = true;
			if (queue) {
				queue = false;
				EventQueue.invokeLater(this);
			}
		}

		// MUST BE THE ONLY METHOD THAT TOUCHES textArea!
		public synchronized void run() {
			int don = 0;

			if (clear) {
				lengths.clear();
				lines.clear();
				textArea.setText("");
				clear = false;
			}

			for (String lin : lines) {
				don += 1;
				lengths.addLast(lin.length());
				if (lengths.size() >= maxLines) {
					textArea.replaceRange("", 0, lengths.removeFirst());
				}
				textArea.append(lin);
				if (don >= 100) {
					break;
				}
			}
			if (don == lines.size()) {
				lines.clear();
				queue = true;
			} else {
				lines.subList(0, don).clear();
				EventQueue.invokeLater(this);
			}
		}

		static private final String EOL1 = "\n";
		static private final String EOL2 = System.getProperty("line.separator", EOL1);
		static private final int LINE_MAX = 1000;
	}

}