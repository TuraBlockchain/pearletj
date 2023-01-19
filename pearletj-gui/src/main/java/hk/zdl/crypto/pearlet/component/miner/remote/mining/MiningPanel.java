package hk.zdl.crypto.pearlet.component.miner.remote.mining;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import org.json.JSONArray;
import org.json.JSONTokener;

import hk.zdl.crypto.pearlet.ui.UIUtil;

public class MiningPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 3870247981006005478L;
	public static final String addational_path = "/api/v1/miner";
	private String basePath = "";
	private final MinerStateTableModel table_model = new MinerStateTableModel();
	private final JTable table = new JTable(table_model);

	public MiningPanel() {
		super(new BorderLayout());
		init_table();
		add(new JScrollPane(table), BorderLayout.CENTER);
	}

	private void init_table() {
		table.setFillsViewportHeight(true);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setColumnSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(false);
		table.setDragEnabled(false);
		table.setRowSelectionAllowed(false);
		table.setShowGrid(true);
		for (var i = 0; i < table_model.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer());
		}
		IntStream.of(1, 5).forEach(i -> table.getColumnModel().getColumn(i).setCellRenderer(new DateCellRenderer()));
		table.getColumnModel().getColumn(10).setCellRenderer(new MinerErrorCellRenderer());
		for (var i = 0; i < table_model.getColumnCount(); i++) {
			((DefaultTableCellRenderer) table.getColumnModel().getColumn(i).getCellRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
		}
		table.getColumnModel().getColumn(4).setCellRenderer(new PlotDirCellRenderer());
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			var in = new URL(basePath + addational_path).openStream();
			var jarr = new JSONArray(new JSONTokener(in));
			in.close();
			table_model.setData(jarr);
		} catch (Exception x) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
		}
		SwingUtilities.invokeLater(() -> UIUtil.adjust_table_width(table, table.getColumnModel()));
	}

}
