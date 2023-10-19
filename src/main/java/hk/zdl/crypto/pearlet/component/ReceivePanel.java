package hk.zdl.crypto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
		setText(e.account);
		Image img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		try {
			String qr_str = "";
			if (e.network.isBurst()) {
				JSONObject jobj = new JSONObject();
				jobj.put("recipient", e.account);
				String str = Base64.encodeBytes(jobj.toString().getBytes());
				qr_str = "signum://v1?action=pay&payload=" + str;
			} else if (e.network.isWeb3J()) {
				qr_str = "ethereum:" + e.account;
			}
			var baos = new ByteArrayOutputStream(5120);
			QRCode.from(qr_str).to(ImageType.GIF).writeTo(baos);
			img = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
			img = makeColorTransparent(img, Color.WHITE);
		} catch (Exception x) {
		}
		qr_code.setIcon(new ImageIcon(img));
	}

	public static Image makeColorTransparent(Image im, final Color color) {
		var filter = new RGBImageFilter() {
			// the color we are looking for... Alpha bits are set to opaque
			public int markerRGB = color.getRGB() | 0xFF000000;

			public final int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				} else {
					// nothing to do
					return rgb;
				}
			}
		};

		var ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}
}
