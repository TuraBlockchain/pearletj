package hk.zdl.crpto.pearlet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Taskbar;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXFrame;

import com.formdev.flatlaf.FlatLightLaf;

public class Main {

	public static void main(String[] args) throws Throwable {
		System.setProperty("apple.awt.application.name", "Pearlet");
		System.setProperty("apple.awt.application.appearance", "system");
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		Taskbar.getTaskbar().setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getClassLoader().getResource("app_icon.png")));
		FlatLightLaf.setup();
		
		var toolbar = new JScrollPane();
		var panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
		for(int i=0;i<100;i++) {
			var btn = new JToggleButton(""+i);
			panel.add(btn);
		}
		toolbar.setViewportView(panel);
		toolbar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		toolbar.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		
		var frame = new JXFrame("Pearlet");
		frame.getContentPane().setLayout(new BorderLayout());
		frame.add(toolbar, BorderLayout.WEST);
		frame.add(new JXButton(), BorderLayout.CENTER);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(800, 600));
		frame.setMinimumSize(new Dimension(800, 600));
		frame.setSize(800, 600);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}

}
