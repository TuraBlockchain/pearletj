package hk.zdl.crypto.pearlet.component.miner;

import static org.jfree.chart.plot.PlotOrientation.HORIZONTAL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.json.JSONObject;
import org.json.JSONTokener;

final class StatusPane extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5037208846880312003L;
	public static final String miner_status_path = "/api/v1/status";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ssXXX");
	private final ChartPanel temp_panel = new ChartPanel(ChartFactory.createBarChart("Temperature(" + (char) 0x2103 + ")", "", "", new DefaultCategoryDataset(), HORIZONTAL, true, true, false));
	private final ChartPanel disk_usage_panel = new ChartPanel(ChartFactory.createPieChart("Disk Usage", new DefaultPieDataset<String>(), true, true, false));
	private final JPanel mining_detail_panel = new JPanel(new BorderLayout(5, 5));
	private final ChartPanel memory_usage_panel = new ChartPanel(ChartFactory.createPieChart("Memory Usage", new DefaultPieDataset<String>(), true, true, false));
	private final DefaultTableModel mining_table_model = new DefaultTableModel(5, 2);
	private final Timer timer = new Timer((int) TimeUnit.SECONDS.toMillis(10), this);
	private JSONObject status;
	private String basePath = "";

	public StatusPane() {
		super(new GridLayout(2, 2));
		Stream.of(temp_panel, disk_usage_panel, mining_detail_panel, memory_usage_panel).forEach(this::add);
		Stream.of(temp_panel, disk_usage_panel, memory_usage_panel).forEach(p -> {
			p.setMouseWheelEnabled(false);
			p.setMouseZoomable(false);
			p.setRangeZoomable(false);
			p.setPopupMenu(null);
			var chart = p.getChart();
			var plot = chart.getPlot();
			var trans = new Color(0, 0, 0, 0);
			chart.getTitle().setFont(MinerGridTitleFont.getFont());
			chart.getTitle().setPaint(getForeground());
			chart.getLegend().setBackgroundPaint(null);
			chart.setBackgroundPaint(trans);
			plot.setBackgroundPaint(trans);
			plot.setOutlinePaint(null);
		});
		init_mining_panel();
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent e) {
				timer.start();
			}
		});
	}

	@SuppressWarnings("serial")
	private void init_mining_panel() {
		mining_detail_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Mining", TitledBorder.CENTER, TitledBorder.TOP, MinerGridTitleFont.getFont()));
		var table = new JTable(mining_table_model) {

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		mining_detail_panel.add(new JScrollPane(table), BorderLayout.CENTER);
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
		plot.setSectionPaint("Plot", Color.blue.darker());
		plot.setSectionPaint("Free", Color.green.darker());
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private void set_memory_usage() {
		var mem = status.getJSONObject("memory");
		long total = mem.getLong("total");
		long free = mem.getLong("free");
		long used = mem.getLong("used");

		var chart = memory_usage_panel.getChart();
		var plot = (PiePlot<String>) chart.getPlot();
		var dataset = new DefaultPieDataset<String>();
		dataset.setValue("Used", used);
		dataset.setValue("Free", free);
		plot.setDataset(dataset);
		plot.setSectionPaint("Used", Color.blue.darker());
		plot.setSectionPaint("Free", Color.green.darker());
	}

	private void set_temp_panel() {
		int cpu_temp = status.getInt("CPU Temp");
		int disk_temp = status.getJSONObject("disk").optInt("temp_cel");
		var chart = temp_panel.getChart();
		var plot = chart.getCategoryPlot();
		var dataset = new DefaultCategoryDataset();
		dataset.addValue(cpu_temp, "CPU", "");
		dataset.addValue(disk_temp, "Disk", "");
		plot.setDataset(dataset);
		plot.getRangeAxis().setRange(0, 100);
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			setStatus(new JSONObject(new JSONTokener(new URL(basePath + miner_status_path).openStream())));
		} catch (Exception x) {
		}
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		timer.stop();
	}

	@Override
	public void repaint() {
		super.repaint();
		var fg = getForeground();
		Stream.of(temp_panel, disk_usage_panel, memory_usage_panel).filter(p -> p != null).forEach(p -> {
			var chart = p.getChart();
			chart.getTitle().setPaint(fg);
			chart.getLegend().setItemPaint(fg);
		});
	}
}