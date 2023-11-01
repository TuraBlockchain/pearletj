package hk.zdl.crypto.pearlet.misc;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class IndepandentWindows {

	private static final List<Window> list = Collections.synchronizedList(new LinkedList<>());

	private IndepandentWindows() {

	}

	public static final void add(Window w) {
		list.add(w);
		w.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				list.remove(w);
			}
		});
	}

	public static final Iterator<Window> iterator() {
		return Collections.unmodifiableList(list).iterator();
	}
}
