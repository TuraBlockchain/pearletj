package hk.zdl.crypto.pearlet.component.commit;

import java.awt.CardLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;

public class CommitPanel extends JPanel {

	private static final long serialVersionUID = -3982073503326811116L;
	private final CardLayout my_card_layout = new CardLayout();
	private final Font my_blod_font = new Font("Arial", Font.BOLD, 48);
	private final JLabel my_jlabel = new JLabel("Commitment Unavailable", JLabel.CENTER);
	private final CommitModifyPanel cmp = new CommitModifyPanel();

	public CommitPanel() {
		setLayout(my_card_layout);
		my_jlabel.setFont(my_blod_font);
		add(my_jlabel, "una");
		my_card_layout.show(this, "una");
		add(cmp, "cmp");
		EventBus.getDefault().register(this);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		if (e.network != null && e.network.isBurst()) {
			cmp.onMessage(e);
			my_card_layout.show(this, "cmp");
		} else {
			my_card_layout.show(this, "una");
		}
	}
}
