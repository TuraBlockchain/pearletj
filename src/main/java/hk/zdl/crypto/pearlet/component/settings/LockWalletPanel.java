package hk.zdl.crypto.pearlet.component.settings;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.event.WalletLockEvent;
import hk.zdl.crypto.pearlet.ui.UIUtil;

public class LockWalletPanel extends JPanel {

	private static final long serialVersionUID = 9007587563161970151L;
	private final JToggleButton button = new JToggleButton();
	private final Icon locked_icon = UIUtil.getStretchIcon("icon/lock-closed.svg", 32, 32);
	private final Icon unlocked_icon = UIUtil.getStretchIcon("icon/lock-open.svg", 32, 32);

	public LockWalletPanel() {
		super(new FlowLayout());
		var panel = new JPanel(new GridLayout(0, 1));
		var panel_1 = new JPanel(new GridBagLayout());
		panel_1.add(new JLabel("Your wallet is now:"), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 2, 2));
		panel_1.add(button, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 2, 2));
		panel_1.add(new JLabel("Lock up your wallet in:"), new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 2, 2));
		var box = new JComboBox<>(Entry.values());
		panel_1.add(box, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 2, 2));
		panel.add(panel_1);
		add(panel);
		EventBus.getDefault().register(this);
		button.addActionListener(e -> EventBus.getDefault().post(new WalletLockEvent(button.isSelected() ? WalletLockEvent.Type.LOCK : WalletLockEvent.Type.UNLOCK)));
		button.setPreferredSize(new Dimension(150, 32));
		button.setHorizontalAlignment(SwingConstants.LEFT);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(WalletLockEvent e) {
		if (e.type == WalletLockEvent.Type.LOCK) {
			button.setIcon(locked_icon);
			button.setText("Locked");
			button.setSelected(true);
		} else {
			button.setIcon(unlocked_icon);
			button.setText("Unlocked");
			button.setSelected(false);
		}
	}

	public enum Entry {
		ONE_MINUTE(1), TEN_MINUTE(10), QUARTER(15), HALF_HOUR(30), NEVER(-1);

		private final int val;

		private Entry(int val) {
			this.val = val;
		}

		public int getValue() {
			return val;
		}

		public String toString() {
			switch (this) {
			case HALF_HOUR:
				return "30 min.";
			case NEVER:
				return "Never";
			case ONE_MINUTE:
				return "1 min.";
			case QUARTER:
				return "15 min.";
			case TEN_MINUTE:
				return "10 min.";
			}
			return "";
		}
	}
}
