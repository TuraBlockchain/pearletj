package hk.zdl.crpto.pearlet.component.dashboard;

import javax.swing.table.TableColumnModel;

import hk.zdl.crpto.pearlet.component.dashboard.signum.InstantCellRenderer;
import hk.zdl.crpto.pearlet.component.dashboard.signum.SignumAddressCellRenderer;
import hk.zdl.crpto.pearlet.component.dashboard.signum.SignumTxTypeCellRenderer;
import hk.zdl.crpto.pearlet.component.dashboard.signum.SignumTxUpdate;
import hk.zdl.crpto.pearlet.component.dashboard.signum.SignumValueCellRenderer;
import hk.zdl.crpto.pearlet.component.dashboard.signum.TxIdCellrenderer;
import hk.zdl.crpto.pearlet.util.CrptoNetworks;

public class DashboardTxProc {

	private final DashboardTxTableModel table_model;

	public DashboardTxProc(DashboardTxTableModel table_model) {
		this.table_model = table_model;
	}

	public void update_data(CrptoNetworks network, String address) throws Exception {
		switch (network) {
		case ROTURA:
			break;
		case SIGNUM:
			new SignumTxUpdate().do_update(table_model, address);
			break;
		case WEB3J:
			break;
		default:
			break;

		}
	}

	public void update_column_model(CrptoNetworks network, TableColumnModel model, String address) throws Exception {
		switch (network) {
		case ROTURA:
			break;
		case SIGNUM:
			model.getColumn(0).setCellRenderer(new TxIdCellrenderer());
			model.getColumn(1).setCellRenderer(new InstantCellRenderer());
			model.getColumn(2).setCellRenderer(new SignumTxTypeCellRenderer());
			model.getColumn(3).setCellRenderer(new SignumValueCellRenderer(address));
			model.getColumn(4).setCellRenderer(new SignumAddressCellRenderer(address));
			break;
		case WEB3J:
			break;
		default:
			break;

		}
	}
}
