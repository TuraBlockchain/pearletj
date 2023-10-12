package hk.zdl.crypto.pearlet.lock;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

import hk.zdl.crypto.pearlet.laf.MyUIManager;

public class Test {

	public static void main(String[] args) throws Throwable {
		MyUIManager.setLookAndFeel();
		var bar = new JProgressBar();
		bar.setPreferredSize(new Dimension(500, 50));
		bar.setIndeterminate(true);
		var d = new JDialog();
		d.getContentPane().add(bar);
		d.pack();
		d.setLocationRelativeTo(null);
		d.setVisible(true);
	}

}
