package hk.zdl.crypto.pearlet.component.miner.remote;

import static org.jfree.chart.plot.PlotOrientation.HORIZONTAL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.json.JSONObject;

import hk.zdl.crypto.pearlet.util.Util;

final class StatusPane extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5037208846880312003L;
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	public static final String miner_status_path = "/api/v1/status";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ssXXX");
	private final ChartPanel temp_panel = new ChartPanel(ChartFactory.createBarChart(MessageFormat.format(rsc_bdl.getString("MINER.REMOTE.STATUS.Temperature"), "" + (char) 0x2103), "", "",
			new DefaultCategoryDataset(), HORIZONTAL, true, true, false));
	private final ChartPanel disk_usage_panel = new ChartPanel(ChartFactory.createPieChart(rsc_bdl.getString("MINER.REMOTE.STATUS.DISK_USAGE"), new DefaultPieDataset<String>(), true, true, false));
	private final JPanel mining_detail_panel = new JPanel(new BorderLayout(5, 5));
	private final ChartPanel memory_usage_panel = new ChartPanel(ChartFactory.createPieChart(rsc_bdl.getString("MINER.REMOTE.STATUS.MEM_USAGE"), new DefaultPieDataset<String>(), true, true, false));
	private final DefaultTableModel mining_table_model = new DefaultTableModel(7, 2);
	private HttpClient client = HttpClient.newHttpClient();
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
	}

	@SuppressWarnings("serial")
	private void init_mining_panel() {
		mining_detail_panel.setBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), rsc_bdl.getString("GENERAL.MINING"), TitledBorder.CENTER, TitledBorder.TOP, MinerGridTitleFont.getFont()));
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
		mining_table_model.setValueAt(rsc_bdl.getString("TABLE.COLUNM_NAME.REMOTE_MINER.STATUS.START_TIME"), 0, 0);
		mining_table_model.setValueAt(rsc_bdl.getString("TABLE.COLUNM_NAME.REMOTE_MINER.STATUS.ACC_LOADED"), 1, 0);
		mining_table_model.setValueAt(rsc_bdl.getString("TABLE.COLUNM_NAME.REMOTE_MINER.STATUS.MINERS_UP"), 2, 0);
		mining_table_model.setValueAt(rsc_bdl.getString("TABLE.COLUNM_NAME.REMOTE_MINER.STATUS.PLOT.FILE_COUNT"), 3, 0);
		mining_table_model.setValueAt(rsc_bdl.getString("TABLE.COLUNM_NAME.REMOTE_MINER.STATUS.PLOT.TOTAL_SIZE"), 4, 0);
		mining_table_model.setValueAt(rsc_bdl.getString("TABLE.COLUNM_NAME.REMOTE_MINER.STATUS.VERSION"), 5, 0);
		mining_table_model.setValueAt(rsc_bdl.getString("TABLE.COLUNM_NAME.REMOTE_MINER.STATUS.BUILD"), 6, 0);
	}

	public void setStatus(JSONObject status) {
		this.status = status;
		set_mining_table();
		set_memory_usage();
		set_disk_usage();
		set_temp_panel();
	}

	private void set_mining_table() {
		var o = new JSONObject();
		var start_time = new Date(status.optLong("start time"));
		mining_table_model.setValueAt(sdf.format(start_time), 0, 1);
		mining_table_model.setValueAt(status.optJSONObject("miner", o).optInt("account count"), 1, 1);
		mining_table_model.setValueAt(status.optJSONObject("miner", o).optInt("active miners"), 2, 1);
		mining_table_model.setValueAt(status.optJSONObject("miner", o).optInt("plot file count"), 3, 1);
		mining_table_model.setValueAt(status.optJSONObject("miner", o).optString("plot file size", "0") + " TiB", 4, 1);
		mining_table_model.setValueAt(status.opt("version"), 5, 1);
		if (status.has("build") && !status.isNull("build")) {
			var x = new Date(status.getLong("build"));
			var sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.SIMPLIFIED_CHINESE);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			mining_table_model.setValueAt(sdf.format(x), 6, 1);
		}
	}

	@SuppressWarnings("unchecked")
	private void set_disk_usage() {
		var o = new JSONObject();
		long total = status.optJSONObject("disk", o).optLong("size");
		long used = status.optJSONObject("disk", o).optLong("used");
		long plot_size = 0;
		long system_used = used - plot_size;

		var chart = disk_usage_panel.getChart();
		PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
		var dataset = new DefaultPieDataset<String>();
		var name_system = rsc_bdl.getString("MINER.REMOTE.STATUS.CHART.LABEL.SYSTEM");
		var name_free = rsc_bdl.getString("MINER.REMOTE.STATUS.CHART.LABEL.FREE");
		var name_plot = rsc_bdl.getString("MINER.REMOTE.STATUS.CHART.LABEL.PLOT");
		dataset.setValue(name_system, system_used);
		dataset.setValue(name_plot, plot_size);
		dataset.setValue(name_free, total - used);
		plot.setDataset(dataset);
		plot.setSectionPaint("Plot", Color.blue.darker());
		plot.setSectionPaint(name_free, Color.green.darker());
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private void set_memory_usage() {
		var mem = status.optJSONObject("memory", new JSONObject());
		long total = mem.optLong("total");
		long free = mem.optLong("free");
		long used = mem.optLong("used");

		var chart = memory_usage_panel.getChart();
		var plot = (PiePlot<String>) chart.getPlot();
		var dataset = new DefaultPieDataset<String>();
		var name_used = rsc_bdl.getString("MINER.REMOTE.STATUS.CHART.LABEL.USED");
		var name_free = rsc_bdl.getString("MINER.REMOTE.STATUS.CHART.LABEL.FREE");
		dataset.setValue(name_used, used);
		dataset.setValue(name_free, free);
		plot.setDataset(dataset);
		plot.setSectionPaint(name_used, Color.blue.darker());
		plot.setSectionPaint(name_free, Color.green.darker());
	}

	private void set_temp_panel() {
		int cpu_temp = Math.max(status.optInt("CPU Temp"), status.optJSONObject("cpu", new JSONObject()).optInt("temp_cel"));
		int disk_temp = status.optJSONObject("disk", new JSONObject()).optInt("temp_cel");
		var chart = temp_panel.getChart();
		var plot = chart.getCategoryPlot();
		var dataset = new DefaultCategoryDataset();
		dataset.addValue(cpu_temp, rsc_bdl.getString("MINER.REMOTE.STATUS.CHART.LABEL.CPU"), "");
		dataset.addValue(disk_temp, rsc_bdl.getString("MINER.REMOTE.STATUS.CHART.LABEL.DISK"), "");
		plot.setDataset(dataset);
		plot.getRangeAxis().setRange(0, 100);
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			var request = HttpRequest.newBuilder().GET().uri(new URL(basePath + miner_status_path).toURI()).build();
			var response = client.send(request, BodyHandlers.ofString());
			setStatus(new JSONObject(response.body()));
		} catch (Exception x) {
		}
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