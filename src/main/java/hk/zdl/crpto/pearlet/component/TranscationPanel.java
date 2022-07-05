package hk.zdl.crpto.pearlet.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crpto.pearlet.component.dashboard.DashboardTxProc;
import hk.zdl.crpto.pearlet.component.dashboard.DashboardTxTableModel;
import hk.zdl.crpto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crpto.pearlet.ui.UIUtil;
import hk.zdl.crpto.pearlet.ui.WaitLayerUI;

@SuppressWarnings("serial")
public class TranscationPanel extends JPanel {
	private final JLayer<JPanel> jlayer = new JLayer<>();
	private final WaitLayerUI wuli = new WaitLayerUI();
	private final DashboardTxTableModel table_model = new DashboardTxTableModel();
	private final TableColumnModel table_column_model = new DefaultTableColumnModel();
	private final DashboardTxProc dbtp = new DashboardTxProc(table_model);
	private final JTable table = new JTable(table_model, table_column_model);
	private Thread tx_histroy_query = null;

	public TranscationPanel() {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
		add(jlayer, BorderLayout.CENTER);
		var _panel = new JPanel(new BorderLayout());
		jlayer.setView(_panel);
		jlayer.setUI(wuli);
		for (int i = 0; i < table_model.getColumnCount(); i++) {
			var tc = new TableColumn(i, 0);
			tc.setHeaderValue(table_model.getColumnName(i));
			table_column_model.addColumn(tc);
		}
		table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
		JScrollPane scrollpane = new JScrollPane(table);
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setShowGrid(true);
		_panel.add(scrollpane, BorderLayout.CENTER);
		var pages_panel = new JPanel(new FlowLayout(2));
		pages_panel.add(new JLabel("Pages:"));
		var page_combobox = new JComboBox<Integer>();
		page_combobox.setEnabled(false);
		pages_panel.add(page_combobox);
		_panel.add(pages_panel, BorderLayout.SOUTH);

		UIUtil.adjust_table_width(table, table_column_model);
		table_model.addTableModelListener(e -> UIUtil.adjust_table_width(table, table_column_model));

	}

	@SuppressWarnings("removal")
	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		if (e.account.equals("null")) {
			return;
		}
		wuli.start();
		if (tx_histroy_query != null) {
			tx_histroy_query.stop();
		}
		tx_histroy_query = new TxTableUpdateThread(e);
		tx_histroy_query.start();

	}

	private final class TxTableUpdateThread extends Thread {
		private final AccountChangeEvent e;

		public TxTableUpdateThread(AccountChangeEvent e) {
			super(TxTableUpdateThread.class.getSimpleName());
			this.e = e;
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				wuli.start();
				table_model.clearData();
				dbtp.update_column_model(e.network, table_column_model, e.account);
				dbtp.update_data(e.network, e.account);
				table.updateUI();
			} catch (ThreadDeath x) {
			} catch (Exception x) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, x.getMessage(), x);
			} finally {
				wuli.stop();
			}
		}

	}

}
