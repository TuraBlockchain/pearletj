package hk.zdl.crypto.pearlet.component;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.ds.RoturaAddress;
import hk.zdl.crypto.pearlet.persistence.MyDb;
import static hk.zdl.crypto.pearlet.util.CrptoNetworks.*;

import hk.zdl.crypto.pearlet.util.CrptoNetworks;
import hk.zdl.crypto.pearlet.util.Util;
import signumj.crypto.SignumCrypto;

@SuppressWarnings("serial")
public class CopyAccountInfoPanel extends JPanel {
	private JButton btn_0, btn_1, btn_2, btn_3,btn_4;
	private List<JButton> btns = new LinkedList<>();
	private CrptoNetworks network;
	private String account;
	private byte[] public_key;

	public CopyAccountInfoPanel() {
		super(new FlowLayout());
		EventBus.getDefault().register(this);
		btn_0 = new JButton("Account ID");
		btn_1 = new JButton("Address");
		btn_2 = new JButton("Extended Address");
		btn_3 = new JButton("Public Key");
		btn_4 = new JButton("More...");
		Stream.of(btn_0, btn_1, btn_2, btn_3,btn_4).forEach(btns::add);
		btns.stream().forEach(this::add);

		btn_0.addActionListener(e -> copy_account_id());
		btn_1.addActionListener(e -> copy_to_clip_board(account));
		btn_2.addActionListener(e -> copy_extended_address());
		btn_3.addActionListener(e -> copy_public_key());
		btn_4.addActionListener(e -> Util.viewAccountDetail(network, account));
	}

	private void copy_account_id() {
		String id = "";
		switch (network) {
		case ROTURA:
		case SIGNUM:
			id = SignumCrypto.getInstance().getAddressFromPublic(public_key).getID();
			copy_to_clip_board(id);
			break;
		case WEB3J:
			id = MyDb.getAccount(network, account).get().getStr("ADDRESS");
			copy_to_clip_board(id);
			break;
		default:
			break;

		}
	}

	private void copy_extended_address() {
		String id = "";
		switch (network) {
		case ROTURA:
			id =  new RoturaAddress(public_key).getExtendedAddress();
			copy_to_clip_board(id);
			break;
		case SIGNUM:
			id = SignumCrypto.getInstance().getAddressFromPublic(public_key).getExtendedAddress();
			copy_to_clip_board(id);
			break;
		case WEB3J:
			id = MyDb.getAccount(network, account).get().getStr("ADDRESS");
			copy_to_clip_board(id);
			break;
		default:
			break;

		}
	}

	private void copy_public_key() {
		String id = "";
		switch (network) {
		case ROTURA:
		case SIGNUM:
			id = SignumCrypto.getInstance().getAddressFromPublic(public_key).getPublicKeyString().toUpperCase();
			copy_to_clip_board(id);
			break;
		case WEB3J:
			id = MyDb.getAccount(network, account).get().getStr("ADDRESS");
			copy_to_clip_board(id);
			break;
		default:
			break;

		}
	}

	private static final void copy_to_clip_board(String str) {
		var s = new StringSelection(str);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, s);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		this.network = e.network;
		this.account = e.account;
		Optional<Record> opt_r = MyDb.getAccount(network, account);
		if(opt_r.isPresent()) {
			public_key = opt_r.get().getBytes("PUBLIC_KEY");
		}
		if(WEB3J.equals(network)) {
			btn_1.setEnabled(true);
			Stream.of(btn_0, btn_2, btn_3,btn_4).forEach(x->x.setEnabled(false));
		}else {
			btns.stream().forEach(o -> o.setEnabled(public_key != null));
		}
	}
}
