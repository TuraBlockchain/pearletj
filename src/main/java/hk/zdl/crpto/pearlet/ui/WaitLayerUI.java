package hk.zdl.crpto.pearlet.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.plaf.LayerUI;

@SuppressWarnings("serial")
public class WaitLayerUI extends LayerUI<JPanel> implements ActionListener {
	private boolean mIsRunning;
	private boolean mIsFadingOut;
	private Timer mTimer;

	private int mAngle;
	private int mFadeCount;
	private int mFadeLimit = 20;

	@SuppressWarnings("unchecked")
	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		JLayer<JPanel> layer = (JLayer<JPanel>) c;
		layer.setLayerEventMask(Long.MAX_VALUE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		JLayer<JPanel> layer = (JLayer<JPanel>) c;
		layer.setLayerEventMask(0);
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		int w = c.getWidth();
		int h = c.getHeight();

		// Paint the view.
		try {
			super.paint(g, c);
		} catch (Exception e) {
		}

		if (!mIsRunning) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g.create();

		float fade = (float) mFadeCount / (float) mFadeLimit;
		// Gray it out.
		Composite urComposite = g2.getComposite();
		float a = fade / 2;
		a = Math.min(a, 1);
		a = Math.max(0, a);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
		g2.setPaint(c.getBackground());
		g2.fillRect(0, 0, w, h);
		g2.setComposite(urComposite);

		// Paint the wait indicator.
		int s = Math.min(w, h) / 5;
		int cx = w / 2;
		int cy = h / 2;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(s / 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.setPaint(Color.gray);
		g2.rotate(Math.PI * mAngle / 180, cx, cy);
		for (int i = 0; i < 12; i++) {
			float scale = (11f - (float) i) / 11f;
			g2.drawLine(cx + s, cy, cx + s * 2, cy);
			g2.rotate(-Math.PI / 6, cx, cy);
			float b = scale * fade;
			b = Math.min(b, 1);
			b = Math.max(0, b);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, b));
		}

		g2.dispose();
	}

	public void actionPerformed(ActionEvent e) {
		synchronized (this) {
			if (mIsRunning) {
				firePropertyChange("tick", 0, 1);
				mAngle += 2;
				if (mAngle >= 360) {
					mAngle = 0;
				}
				if (mIsFadingOut) {
					if (--mFadeCount <= 0) {
						mIsRunning = false;
						mTimer.stop();
					}
				} else if (mFadeCount < mFadeLimit) {
					mFadeCount++;
				}
			}
		}
	}

	public void start() {
		synchronized (this) {
			// Run a thread for animation.
			mIsRunning = true;
			mIsFadingOut = false;
			int fps = 40;
			int tick = 1000 / fps;
			if (mTimer == null) {
				mTimer = new Timer(tick, this);
			}
			if (!mTimer.isRunning()) {
				mTimer.start();
			}
		}
	}

	public void stop() {
		synchronized (this) {
			mIsFadingOut = true;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void applyPropertyChange(PropertyChangeEvent pce, JLayer l) {
		if ("tick".equals(pce.getPropertyName())) {
			l.repaint();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void processMouseEvent(MouseEvent e, JLayer l) {
		if (mIsRunning) {
			e.consume();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void processMouseMotionEvent(MouseEvent e, JLayer l) {
		if (mIsRunning) {
			e.consume();
		}
	}

}