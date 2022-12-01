package hk.zdl.crypto.pearlet.component.miner.local;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class StartPanel extends JPanel {

	private static final Insets insets_5 = new Insets(5, 5, 5, 5);
	private static final long serialVersionUID = 1278363752513931443L;
	private final JList<String> path_list = new JList<>(new DefaultListModel<String>());

	public StartPanel() {
		super(new BorderLayout());
		JScrollPane scr = new JScrollPane(path_list);
		scr.setBorder(BorderFactory.createTitledBorder("Miner Paths"));
		add(scr, BorderLayout.CENTER);

		var btn_panel = new JPanel(new GridBagLayout());
		var add_btn = new JButton("Add");
		btn_panel.add(add_btn, new GridBagConstraints(0, 0, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var del_btn = new JButton("Delete");
		btn_panel.add(del_btn, new GridBagConstraints(0, 1, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));
		var run_btn = new JButton("Run");
		btn_panel.add(run_btn, new GridBagConstraints(0, 2, 1, 1, 0, 0, 10, 0, insets_5, 0, 0));

		var panel_1 = new JPanel(new FlowLayout(1, 0, 0));
		panel_1.add(btn_panel);
		add(panel_1, BorderLayout.EAST);

		add_btn.addActionListener((e) -> {
			var file_dialog = new JFileChooser();
			file_dialog.setDialogType(JFileChooser.OPEN_DIALOG);
			file_dialog.setMultiSelectionEnabled(false);
			file_dialog.setDragEnabled(false);
			file_dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int i = file_dialog.showOpenDialog(getRootPane());
			if (i != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = file_dialog.getSelectedFile();
			((DefaultListModel<String>) path_list.getModel()).addElement(file.getAbsolutePath());
		});

		del_btn.addActionListener(e -> {
			var model = (DefaultListModel<String>) path_list.getModel();
			int i = path_list.getSelectedIndex();
			if (i > -1) {
				model.remove(i);
			}
		});

		run_btn.addActionListener(e -> {
			if (((DefaultListModel<String>) path_list.getModel()).size() < 1) {
				return;
			}
		});
	}

}
