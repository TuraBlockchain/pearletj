package hk.zdl.crypto.pearlet.component.blocks;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import hk.zdl.crypto.pearlet.component.event.BlockEvent;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;
import hk.zdl.crypto.pearlet.ui.UIUtil;
import hk.zdl.crypto.pearlet.ui.WaitLayerUI;
import hk.zdl.crypto.pearlet.util.CryptoUtil;
import hk.zdl.crypto.pearlet.util.Util;

public class BlocksPanel extends JPanel {

	private static final ExecutorService es = Executors.newFixedThreadPool(10, (r) -> {
		var t = new Thread(r, "");
		t.setDaemon(true);
		return t;
	});

	private static final long serialVersionUID = 1455088889510667002L;
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
				Point point = mouseEvent.getPoint();
				int row = table.rowAtPoint(point);
				if (mouseEvent.getClickCount() == 2 && row >= 0 & row == table.getSelectedRow()) {
					try {
						Util.viewBlockDetail(nw, table_model.getValueAt(row, 0).toString());
					} catch (Exception x) {
						Logger.getLogger(getClass().getName()).log(Level.WARNING, x.getMessage(), x);
					}
				}
			}
		});
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(AccountChangeEvent e) {
		this.nw = e.network;
		this.account = e.account;
		if (nw == null || e.account == null || e.account.isBlank()) {
			return;
		}
		es.submit(() -> {
			try {
				EventBus.getDefault().post(new BlockEvent<>(e.network, BlockEvent.Type.START, null));
				if (!nw.isBurst()) {
					return;
				}
				var jarr = CryptoUtil.getSignumBlockID(nw, account, 0, 0);
				if (nw.equals(e.network) && account.equals(e.account)) {
					for (int i = 0; i < jarr.length(); i++) {
						var str = jarr.getString(i);
						table_model.insertData(new Object[] { str, null, null, null, null });
						es.submit(new BlockQuery(nw, account, str, i));
					}
				}
			} catch (Exception x) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, x.getMessage(), x);
			} finally {
				EventBus.getDefault().post(new BlockEvent<>(e.network, BlockEvent.Type.FINISH, null));
			}
		});
		table_column_model.getColumn(0).setCellRenderer(new RightAlignCellRanderer());
		table_column_model.getColumn(1).setCellRenderer(new RightAlignCellRanderer());
		table_column_model.getColumn(2).setCellRenderer(new InstantCellRenderer(e.network));
		table_column_model.getColumn(3).setCellRenderer(new SignumValueCellRenderer(e.network));
		table_column_model.getColumn(4).setCellRenderer(new RightAlignCellRanderer());
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void onMessage(BlockEvent<?> e) {
		switch (e.type) {
		case START:
			wuli.start();
			table_model.clearData();
			break;
		case FINISH:
			wuli.stop();
			break;
		case INSERT:
			Object o = e.data;
			table_model.insertData(new Object[] { o, null, null, null, null });
			break;
		default:
			break;

		}
	}

	private class BlockQuery implements Runnable {

		private final CryptoNetwork my_nw;
		private final String my_account, my_block_id;
		private final int index;

		private BlockQuery(CryptoNetwork my_nw, String my_account, String my_block_id, int index) {
			super();
			this.my_nw = my_nw;
			this.my_account = my_account;
			this.my_block_id = my_block_id;
			this.index = index;
		}

		@Override
		public void run() {
			try {
				var jobj = CryptoUtil.getSignumBlock(my_nw, my_block_id);
				SwingUtilities.invokeLater(()->{
					long height = jobj.getLong("height");
					long timestamp = jobj.getLong("timestamp");
					long reward = jobj.getLong("blockRewardNQT");
					int notx = jobj.getInt("numberOfTransactions");
					if (my_nw.equals(nw) && my_account.equals(account) && table_model.getValueAt(index, 0).equals(my_block_id)) {
						table_model.setValueAt(height, index, 1);
						table_model.setValueAt(timestamp, index, 2);
						table_model.setValueAt(reward, index, 3);
						table_model.setValueAt(notx, index, 4);
					}
				});
			} catch (Exception x) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, x.getMessage(), x);
			} finally {
			}
		}

	}
	
	private static class RightAlignCellRanderer extends DefaultTableCellRenderer{
		private static final long serialVersionUID = 8124066930956045072L;

		public RightAlignCellRanderer() {
			setHorizontalAlignment(SwingConstants.RIGHT);
		}
	}
}
