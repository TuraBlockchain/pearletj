package hk.zdl.crypto.pearlet.component.blocks;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hk.zdl.crypto.pearlet.component.event.AccountChangeEvent;
import hk.zdl.crypto.pearlet.component.settings.DisplaySettings;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.ui.WaitLayerUI;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class BlocksPanel extends JPanel {

	private static final long serialVersionUID = 1455088889510667002L;
	private final Object lock = new Object();
	private final JLayer<JPanel> jlayer = new JLayer<>();
	private final WaitLayerUI wuli = new WaitLayerUI();
	private final BlocksTableModel table_model = new BlocksTableModel();
	private final TableColumnModel table_column_model = new DefaultTableColumnModel();
	private final JTable table = new JTable(table_model, table_column_model);
	private CryptoNetwork nw;
	private String account;

	public BlocksPanel() {
		super(new BorderLayout());
		EventBus.getDefault().register(this);
		add(jlayer, BorderLayout.CENTER);
		for (int i = 0; i < table_model.getColumnCount(); i++) {
			var tc = new TableColumn(i, 0);
			tc.setHeaderValue(table_model.getColumnName(i));
			table_column_model.addColumn(tc);
		}
		table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setShowGrid(true);
		var _panel = new JPanel(new BorderLayout());
		_panel.add(new JScrollPane(table), BorderLayout.CENTER);
		jlayer.setView(_panel);
		jlayer.setUI(wuli);

		UIUtil.adjust_table_width(table, table_column_model);
		table_model.addTableModelListener(new TableModelListener() {
			private long _last_table_update;

			@Override
			public void tableChanged(TableModelEvent e) {
				if (System.currentTimeMillis() - _last_table_update > 1000) {
					SwingUtilities.invokeLater(() -> {
						UIUtil.adjust_table_width(table, table_column_model);
						_last_table_update = System.currentTimeMillis();
					});

				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				if (!nw.isBurst()) {
					return;
				}
				Point point = mouseEvent.getPoint();
				int row = table.rowAtPoint(point);
				if (mouseEvent.getClickCount() == 2 && row >= 0 & row == table.getSelectedRow()) {
					try {
						var o = table_model.getValueAt(row, 1);
						if (o != null) {
							Util.viewBlockDetail(nw, o.toString());
						}
					} catch (Exception x) {
						Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
					}
				}
			}
		});
		Util.submit(new BlockQuery());
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		this.nw = e.network;
		this.account = e.account;
		if (nw == null || e.account == null || e.account.isBlank()) {
			return;
		}
		try {
			wuli.start();
			synchronized (lock) {
				table_model.clearData();
				if (!nw.isBurst()) {
					return;
				}
				var jarr = CryptoUtil.getSignumBlockID(nw, account, 0, 0);
				var limit = Util.getUserSettings().getInt(DisplaySettings.BLOCK_COUNT, 100);
				for (int i = 0; i < jarr.length() && i < limit; i++) {
					var str = jarr.getString(i);
					table_model.insertData(new Object[] { str, null, null, null, null });
				}
			}
		} catch (RuntimeException | SocketTimeoutException | InterruptedException | ThreadDeath x) {
		} catch (Exception x) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, x.getMessage(), x);
		} finally {
			wuli.stop();
		}
		table_column_model.getColumn(0).setCellRenderer(new RightAlignCellRanderer());
		table_column_model.getColumn(1).setCellRenderer(new RightAlignCellRanderer());
		table_column_model.getColumn(2).setCellRenderer(new InstantCellRenderer(e.network));
		table_column_model.getColumn(3).setCellRenderer(new SignumValueCellRenderer(e.network));
		table_column_model.getColumn(4).setCellRenderer(new RightAlignCellRanderer());
	}

	private class BlockQuery implements Runnable {
		private int i = 0;

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(50);
					if (i >= table_model.getRowCount()) {
						i = 0;
						continue;
					}
					synchronized (lock) {
						try {
							var block_id = table_model.getValueAt(i, 0).toString();
							var jobj = BlockCache.getSignumBlock(nw, block_id);
							long height = jobj.getLong("height");
							long timestamp = jobj.getLong("timestamp");
							long reward = jobj.getLong("blockRewardNQT");
							int notx = jobj.getInt("numberOfTransactions");
							table_model.setValueAt(height, i, 1);
							table_model.setValueAt(timestamp, i, 2);
							table_model.setValueAt(reward, i, 3);
							table_model.setValueAt(notx, i, 4);
						} catch (SocketTimeoutException | InterruptedException | ThreadDeath x) {
						}
					}
					i++;
				} catch (Exception x) {
					Logger.getLogger(getClass().getName()).log(Level.SEVERE, x.getMessage(), x);
				}
			}
		}

	}

	private static class RightAlignCellRanderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 8124066930956045072L;

		public RightAlignCellRanderer() {
			setHorizontalAlignment(SwingConstants.RIGHT);
		}
	}
}
