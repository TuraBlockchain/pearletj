package hk.zdl.crypto.pearlet.laf;

import java.awt.Toolkit;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLaf;
import com.jthemedetecor.OsThemeDetector;

import hk.zdl.crypto.pearlet.misc.IndepandentWindows;

public class MyUIManager {

	private static final Map<String, String> sound_map = new TreeMap<>();
	static {
		sound_map.put("OptionPane.informationSound", "win.sound.asterisk");
		sound_map.put("OptionPane.questionSound", "win.sound.question");
		sound_map.put("OptionPane.warningSound", "win.sound.exclamation");
		sound_map.put("OptionPane.errorSound", "win.sound.hand");
		OsThemeDetector.getDetector().registerListener((isDark) -> {
			setLookAndFeel(isDark);
		});
	}

	public static final void setLookAndFeel() {
		var isDark = OsThemeDetector.getDetector().isDark();
		setLookAndFeel(isDark);
	}

	private static final void setLookAndFeel(boolean isDark) {
		FlatLaf.setup(isDark ? new FlatWinDarkLaf() : new FlatWinLightLaf());
		SwingUtilities.invokeLater(() -> {
			IndepandentWindows.iterator().forEachRemaining(SwingUtilities::updateComponentTreeUI);
		});
	}

	protected static void playSound(Action audioAction) {
		String actionName = (String) audioAction.getValue(Action.NAME);
		Runnable runnable = (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty(sound_map.get(actionName));
		if (runnable != null)
			runnable.run();
	}
}
