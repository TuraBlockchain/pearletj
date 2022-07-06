package hk.zdl.crpto.pearlet.component;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.jfinal.plugin.activerecord.Record;

import hk.zdl.crpto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crpto.pearlet.ds.RoturaAddress;
import hk.zdl.crpto.pearlet.persistence.MyDb;
import hk.zdl.crpto.pearlet.util.CrptoNetworks;
import hk.zdl.crpto.pearlet.util.Util;
import signumj.crypto.SignumCrypto;

@SuppressWarnings("serial")
public class CopyAccountInfoPanel extends JPanel {
	private List<JButton> btns = new LinkedList<>();
	private CrptoNetworks network;
	private String account;
	private byte[] public_key;

	public CopyAccountInfoPanel() {
		super(new FlowLayout());
		EventBus.getDefault().register(this);
		var btn_0 = new JButton("Account ID");
		var btn_1 = new JButton("Address");
		var btn_2 = new JButton("Extended Address");
		var btn_3 = new JButton("Public Key");
		var btn_4 = new JButton("More...");
		Stream.of(btn_0, btn_1, btn_2, btn_3,btn_4).forEach(btns::add);
		btns.stream().forEach(this::add);

		btn_0.addActionListener(e -> copy_account_id());
		btn_1.addActionListener(e -> copy_to_clip_board(account));
		btn_2.addActionListener(e -> copy_extended_address());
		btn_3.addActionListener(e -> copy_public_key());
		btn_4.addActionListener(e -> Util.viewAccountDetail(network, account));
	}

	private void copy_account_id() {
		switch (network) {
		case ROTURA:
		case SIGNUM:
			String id = SignumCrypto.getInstance().getAddressFromPublic(public_key).getID();
			copy_to_clip_board(id);
			break;
		case WEB3J:
			break;
		default:
			break;

		}
	}

	private void copy_extended_address() {
		String id;
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
			break;
		default:
			break;

		}
	}

	private void copy_public_key() {
		switch (network) {
		case ROTURA:
		case SIGNUM:
			String id = SignumCrypto.getInstance().getAddressFromPublic(public_key).getPublicKeyString().toUpperCase();
			copy_to_clip_board(id);
			break;
		case WEB3J:
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

		public_key = null;
		List<Record> l = MyDb.getAccounts(network);
		for (var r : l) {
			if (network.equals(CrptoNetworks.SIGNUM)) {
				var adr = SignumCrypto.getInstance().getAddressFromPublic(r.getBytes("PUBLIC_KEY"));
				if (adr.getFullAddress().equals(account)) {
					public_key = adr.getPublicKey();
					break;
				}
			}else if(network.equals(CrptoNetworks.ROTURA)) {
				var adr = new RoturaAddress(r.getBytes("PUBLIC_KEY"));
				if (adr.getFullAddress().equals(account)) {
					public_key = adr.getPublicKey();
					break;
				}
			}
		}
		btns.stream().forEach(o -> o.setEnabled(public_key != null));
	}
}
