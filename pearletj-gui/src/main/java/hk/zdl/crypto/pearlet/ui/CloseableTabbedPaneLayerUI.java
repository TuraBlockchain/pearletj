package hk.zdl.crypto.pearlet.ui;

import java.awt.AWTEvent;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.plaf.LayerUI;

public class CloseableTabbedPaneLayerUI extends LayerUI<JTabbedPane> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4341038860689931620L;
	private JTabbedPane target_view_pane;

	@SuppressWarnings("unchecked")
	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		JLayer<JTabbedPane> layer = (JLayer<JTabbedPane>) c;
		layer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
		target_view_pane = layer.getView();
	}

	@Override
	public void uninstallUI(JComponent c) {
		((JLayer<?>) c).setLayerEventMask(0);
		super.uninstallUI(c);
	}

	@Override
	protected void processMouseEvent(MouseEvent e, JLayer<? extends JTabbedPane> l) {
		if (e.getID() == MouseEvent.MOUSE_CLICKED && e.getClickCount() == 1 && e.getButton() != 1) {
			JTabbedPane tabbedPane = l.getView();
			if(!target_view_pane.equals(e.getSource())) {
				return;
			}
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				Rectangle rect = tabbedPane.getBoundsAt(i);
				if (rect == null) {
					continue;
				}
				if (rect.contains(e.getPoint())) {
					e.consume();
					JPopupMenu pop = new JPopupMenu();
					JMenuItem item = new JMenuItem("Close Tab");
					pop.add(item);
					int j = i;
					item.addActionListener(o -> tabbedPane.removeTabAt(j));
					pop.show(l, e.getX(), e.getY());
				}
			}
			l.getView().repaint();
		}
	}

}