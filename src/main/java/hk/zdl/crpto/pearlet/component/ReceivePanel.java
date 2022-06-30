package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ReceivePanel extends JPanel {

	private final JTextField adr_filed = new JTextField();

	public ReceivePanel() {
		super(new FlowLayout());
		var panel = new JPanel(new BorderLayout(5, 5));
		adr_filed.setMinimumSize(new Dimension(400, 20));
		adr_filed.setPreferredSize(new Dimension(400, 20));
		panel.add(adr_filed, BorderLayout.CENTER);
		var btn = new JButton("Copy Address");
		panel.add(btn, BorderLayout.EAST);
		add(panel);
		adr_filed.setEditable(false);
		btn.addActionListener(e -> {
			var s = new StringSelection(adr_filed.getText().trim());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, s);
			//It works!
		});
//		setText("ha!ha!");
	}

	public void setText(String t) {
		adr_filed.setText(t);
	}

}
