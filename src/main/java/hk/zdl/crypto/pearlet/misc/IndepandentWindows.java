package hk.zdl.crypto.pearlet.misc;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class IndepandentWindows {

	private static final IndepandentWindows INSTANCE = new IndepandentWindows();

	private final List<Window> list = new LinkedList<>();

	private IndepandentWindows() {

	}


	public static final void add(Window w) {
		INSTANCE.list.add(w);
		w.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				synchronized (INSTANCE.list) {
					INSTANCE.list.remove(w);
				}
			}
		});
	}

	public static final Iterator<Window> iterator() {
		return Collections.unmodifiableList(INSTANCE.list).iterator();
	}
}
