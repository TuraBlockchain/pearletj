package hk.zdl.crypto.pearlet.component.miner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.json.JSONObject;

final class StatusPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5037208846880312003L;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSXXX");
	private static final Font title_font = new Font("Ariel Rounded", Font.PLAIN, 24);
	private final ChartPanel temp_panel = new ChartPanel(ChartFactory.createBarChart("Temperature(" + (char) 0x2103 + ")", "", "", new DefaultCategoryDataset(), PlotOrientation.HORIZONTAL, true, true, false));
	private final ChartPanel disk_usage_panel = new ChartPanel(ChartFactory.createPieChart("Disk Usage", new DefaultPieDataset<String>(), true, true, false));
	private final JPanel mining_detail_panel = new JPanel(new BorderLayout());
	private final ChartPanel memory_usage_panel = new ChartPanel(ChartFactory.createPieChart("Memory Usage", new DefaultPieDataset<String>(), true, true, false));
	private final DefaultTableModel mining_table_model = new DefaultTableModel(5, 2);
	private JSONObject status;

	public StatusPane() {
		super(new GridLayout(2, 2));
		Stream.of(temp_panel, disk_usage_panel, mining_detail_panel, memory_usage_panel).forEach(this::add);
		init_mining_panel();
	}

	private void init_mining_panel() {
		var mining_title_label = new JLabel("Mining");
		mining_title_label.setFont(title_font);
		mining_title_label.setHorizontalAlignment(SwingConstants.CENTER);
		mining_detail_panel.add(mining_title_label, BorderLayout.NORTH);
		var table = new JTable(mining_table_model) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 3693212988335453847L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		var scr = new JScrollPane(table);
		mining_detail_panel.add(scr, BorderLayout.CENTER);
		table.getTableHeader().setReorderingAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(false);
		table.setDragEnabled(false);
		table.setRowSelectionAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setTableHeader(null);
		table.setShowGrid(true);
		mining_table_model.setValueAt("Start Time", 0, 0);
		mining_table_model.setValueAt("Block Height", 1, 0);
		mining_table_model.setValueAt("Scoop", 2, 0);
		mining_table_model.setValueAt("Speed", 3, 0);
		mining_table_model.setValueAt("Round Time", 4, 0);
	}

	public void setStatus(JSONObject status) {
		this.status = status;
		set_mining_table();
		set_memory_usage();
		set_disk_usage();
		set_temp_panel();
	}

	private void set_mining_table() {
		mining_table_model.setValueAt(status.getJSONObject("miner").get("height"), 1, 1);
		mining_table_model.setValueAt(status.getJSONObject("miner").get("scoop"), 2, 1);
		mining_table_model.setValueAt(status.getJSONObject("miner").get("speed"), 3, 1);
		mining_table_model.setValueAt(status.getJSONObject("miner").get("roundtime"), 4, 1);
		var start_time = new Date(status.getLong("start_time"));
		mining_table_model.setValueAt(sdf.format(start_time), 0, 1);
	}

	@SuppressWarnings("unchecked")
	private void set_disk_usage() {
		long total = status.getJSONObject("disk").getLong("size");
		long used = status.getJSONObject("disk").getLong("used");
		var _plot_size = status.getJSONObject("miner").getString("total capacity").replace(" TiB", "").trim();
		long plot_size = new BigDecimal(_plot_size).multiply(new BigDecimal("2").pow(30)).longValue();
		long system_used = used - plot_size;

		var chart = disk_usage_panel.getChart();
		PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
		var dataset = new DefaultPieDataset<String>();
		dataset.setValue("System", system_used);
		dataset.setValue("Plot", plot_size);
		dataset.setValue("Free", total - used);
		plot.setDataset(dataset);
		var trans = new Color(0xFF, 0xFF, 0xFF, 0);
		chart.setBackgroundPaint(trans);
		plot.setBackgroundPaint(trans);
	}

	@SuppressWarnings("unchecked")
	private void set_memory_usage() {
		var mem = status.getJSONObject("memory");
		long total = mem.getLong("total");
		long free = mem.getLong("free");
		long used = mem.getLong("used");

		var chart = memory_usage_panel.getChart();
		PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
		var dataset = new DefaultPieDataset<String>();
		dataset.setValue("Used", used);
		dataset.setValue("Free", free);
		plot.setDataset(dataset);
		var trans = new Color(0xFF, 0xFF, 0xFF, 0);
		chart.setBackgroundPaint(trans);
		plot.setBackgroundPaint(trans);
	}
	private void set_temp_panel() {
		int cpu_temp = status.getInt("CPU Temp");
		int disk_temp = status.getJSONObject("disk").getInt("temp_cel");
		var chart = temp_panel.getChart();
		var plot = chart.getCategoryPlot();
		var dataset = new DefaultCategoryDataset();
		dataset.addValue(cpu_temp, "CPU", "");
		dataset.addValue(disk_temp, "Disk", "");
		plot.setDataset(dataset);
		plot.getRangeAxis().setRange(0, 100);
		var trans = new Color(0xFF, 0xFF, 0xFF, 0);
		chart.setBackgroundPaint(trans);
		plot.setBackgroundPaint(trans);
	}
}