package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import hk.zdl.crpto.pearlet.MyToolbar;

@SuppressWarnings("serial")
public class MessagesPanel extends JSplitPane {

	public MessagesPanel() {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		setOneTouchExpandable(true);
		setDividerLocation(200);

		var left_panel = new JPanel(new BorderLayout());
		var start_conv_btn = new JButton("New Message");
		left_panel.add(start_conv_btn, BorderLayout.NORTH);
		var conv_scr = new JScrollPane();
		left_panel.add(conv_scr, BorderLayout.CENTER);
		setTopComponent(left_panel);

		setBottomComponent(getDefaultRightComponent());
	}

	private static final Component getDefaultRightComponent() {
		var my_panel = new JPanel(new GridBagLayout());
		try {
			var my_icon = new MyStretchIcon(ImageIO.read(MyToolbar.class.getClassLoader().getResource("toolbar/" + "chat-text.svg")), 256, 256);
			my_panel.add(new JLabel(my_icon), new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
		} catch (IOException e) {
		}
		var label_0 = new JLabel("Messages");
		label_0.setFont(new Font("Arial Black", Font.PLAIN, 28));
		label_0.setHorizontalAlignment(SwingConstants.CENTER);
		var label_1 = new JLabel("Select a contact to start a message");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		my_panel.add(label_0, new GridBagConstraints(0, 1, 1, 1, 0, 0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
		my_panel.add(label_1, new GridBagConstraints(0, 2, 1, 1, 0, 0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
		return my_panel;
	}

}
