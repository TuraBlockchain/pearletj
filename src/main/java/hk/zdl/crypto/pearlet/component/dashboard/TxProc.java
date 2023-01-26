package hk.zdl.crypto.pearlet.component.dashboard;

import javax.swing.table.TableColumnModel;

import hk.zdl.crypto.pearlet.component.dashboard.ether.EtherAddressCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.ether.EtherTxDateCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.ether.EtherTxIdCellRanderer;
import hk.zdl.crypto.pearlet.component.dashboard.ether.EtherTxTypeCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.ether.EtherValueCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.rotura.RoturaAddressCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.rotura.RoturaInstantCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.rotura.RoturaValueCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.signum.InstantCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.signum.SignumAddressCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.signum.SignumTxTypeCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.signum.SignumValueCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.signum.TxIdCellrenderer;
import hk.zdl.crypto.pearlet.util.CrptoNetworks;

public class TxProc {

	public void update_column_model(CrptoNetworks network, TableColumnModel model, String address){
		switch (network) {
		case ROTURA:
			model.getColumn(0).setCellRenderer(new TxIdCellrenderer());
			model.getColumn(1).setCellRenderer(new RoturaInstantCellRenderer());
			model.getColumn(2).setCellRenderer(new SignumTxTypeCellRenderer());
			model.getColumn(3).setCellRenderer(new RoturaValueCellRenderer(address));
			model.getColumn(4).setCellRenderer(new RoturaAddressCellRenderer(address));
			break;
		case SIGNUM:
			model.getColumn(0).setCellRenderer(new TxIdCellrenderer());
			model.getColumn(1).setCellRenderer(new InstantCellRenderer());
			model.getColumn(2).setCellRenderer(new SignumTxTypeCellRenderer());
			model.getColumn(3).setCellRenderer(new SignumValueCellRenderer(address));
			model.getColumn(4).setCellRenderer(new SignumAddressCellRenderer(address));
			break;
		case WEB3J:
			model.getColumn(0).setCellRenderer(new EtherTxIdCellRanderer());
			model.getColumn(1).setCellRenderer(new EtherTxDateCellRenderer());
			model.getColumn(2).setCellRenderer(new EtherTxTypeCellRenderer());
			model.getColumn(3).setCellRenderer(new EtherValueCellRenderer(address));
			model.getColumn(4).setCellRenderer(new EtherAddressCellRenderer(address));
			break;
		default:
			break;

		}
	}
}
