package hk.zdl.crypto.pearlet.laf;

import javax.swing.Action;

import com.formdev.flatlaf.FlatLightLaf;

public class FlatWinLightLaf extends FlatLightLaf {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3477409107096133974L;

	@Override
	protected void playSound(Action audioAction) {
		MyUIManager.playSound(audioAction);
	}

}
