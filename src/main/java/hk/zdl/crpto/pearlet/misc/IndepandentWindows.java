package hk.zdl.crpto.pearlet.misc;

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

	public static final IndepandentWindows getInstance() {
		return INSTANCE;
	}

	public void add(Window w) {
		list.add(w);
		w.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				synchronized (list) {
					list.remove(w);
				}
			}
		});
	}

	public Iterator<Window> iterator() {
		return Collections.unmodifiableList(list).iterator();
	}
}
