package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.java_websocket.util.Base64;
import org.json.JSONObject;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

@SuppressWarnings("serial")
public class ReceivePanel extends JPanel {

	private final JTextField adr_filed = new JTextField();
	private final JLabel qr_code = new JLabel() {

		@Override
		public void paint(Graphics g) {
			if (getIcon() != null) {
				Image img = ((ImageIcon) getIcon()).getImage();
				int w = getWidth();
				int h = getHeight();
				int c = Math.min(w, h);
				g.drawImage(img, (w - c) / 2, (h - c) / 2, c, c, null);
			} else {
				super.paint(g);
			}
		}
	};

	public ReceivePanel() {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
		var panel_0 = new JPanel(new BorderLayout());
		var panel_1 = new JPanel(new FlowLayout());
		adr_filed.setMinimumSize(new Dimension(400, 20));
		adr_filed.setPreferredSize(new Dimension(400, 20));
		adr_filed.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
		panel_0.add(adr_filed, BorderLayout.CENTER);
		var btn = new JButton("Copy Address");
		panel_0.add(btn, BorderLayout.EAST);
		panel_1.add(panel_0);
		add(panel_1, BorderLayout.NORTH);
		add(qr_code, BorderLayout.CENTER);
		adr_filed.setEditable(false);
		qr_code.setHorizontalAlignment(SwingConstants.CENTER);
		btn.addActionListener(e -> {
			var s = new StringSelection(adr_filed.getText().trim());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, s);
			// It works!
		});
	}

	public void setText(String t) {
		adr_filed.setText(t);
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		if (e.account == null)
			return;
		setText(e.account);
		String qr_str = "";
		if (e.network.isBurst()) {
			JSONObject jobj = new JSONObject();
			jobj.put("recipient", e.account);
			String str = Base64.encodeBytes(jobj.toString().getBytes());
			qr_str = "signum://v1?action=pay&payload=" + str;
		} else if (e.network.isWeb3J()) {
			qr_str = "ethereum:" + e.account;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream(5120);
		QRCode.from(qr_str).to(ImageType.GIF).writeTo(baos);
		BufferedImage img = null;
		try {
			img = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
		} catch (IOException e1) {
		}
		qr_code.setIcon(new ImageIcon(img));
	}
}
