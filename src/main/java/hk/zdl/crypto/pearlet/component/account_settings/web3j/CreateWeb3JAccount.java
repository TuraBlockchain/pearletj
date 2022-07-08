package hk.zdl.crypto.pearlet.component.account_settings.web3j;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import hk.zdl.crypto.pearlet.component.MyStretchIcon;
import hk.zdl.crypto.pearlet.misc.IndepandentWindows;
import hk.zdl.crypto.pearlet.util.Util;

public class CreateWeb3JAccount {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	public static final void create_new_account_dialog(Component c) {
		var w = SwingUtilities.getWindowAncestor(c);
		var dialog = new JDialog(w, "Create New Account", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		IndepandentWindows.add(dialog);
		var panel = new JPanel(new GridBagLayout());
		try {
			panel.add(new JLabel(new MyStretchIcon(ImageIO.read(Util.getResource("icon/" + "cloud-plus-fill.svg")), 64, 64)), new GridBagConstraints(0, 0, 1, 4, 0, 0, 17, 0, insets_5, 0, 0));
		} catch (IOException e) {
		}
		var label_1 = new JLabel("Please setup password for your new account:");
		var pw_field = new JPasswordField(30);

	}
}
