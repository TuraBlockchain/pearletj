package hk.zdl.crypto.pearlet.laf;

import javax.swing.Action;

import com.formdev.flatlaf.FlatDarkLaf;

public class FlatWinDarkLaf extends FlatDarkLaf {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1720602591636520516L;

	@Override
	protected void playSound(Action audioAction) {
		MyUIManager.playSound(audioAction);
	}

}
