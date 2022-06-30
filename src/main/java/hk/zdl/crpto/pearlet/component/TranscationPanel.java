package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class TranscationPanel extends JPanel {

	public TranscationPanel() {
		super(new BorderLayout());
		var table = new JTable(5,5);
		JScrollPane scrollpane = new JScrollPane(table);
		add(scrollpane,BorderLayout.CENTER);
		var pages_panel = new JPanel(new FlowLayout(2));
		pages_panel.add(new JLabel("Pages:"));
		var page_combobox = new JComboBox();
		pages_panel.add(page_combobox);
		add(pages_panel,BorderLayout.SOUTH);
	}

}
