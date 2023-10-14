package hk.zdl.crypto.pearlet.component.dashboard;

import javax.swing.table.TableColumnModel;

import hk.zdl.crypto.pearlet.component.dashboard.ether.EtherAddressCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.ether.EtherTxDateCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.ether.EtherTxIdCellRanderer;
import hk.zdl.crypto.pearlet.component.dashboard.ether.EtherTxTypeCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.ether.EtherValueCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.signum.InstantCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.signum.SignumAddressCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.signum.SignumTxTypeCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.signum.SignumValueCellRenderer;
import hk.zdl.crypto.pearlet.component.dashboard.signum.TxIdCellrenderer;
import hk.zdl.crypto.pearlet.ds.CryptoNetwork;

public class TxProc {

	public void update_column_model(CryptoNetwork network, TableColumnModel model, String address) {
		if (network.isWeb3J()) {
			model.getColumn(0).setCellRenderer(new EtherTxIdCellRanderer());
			model.getColumn(1).setCellRenderer(new EtherTxDateCellRenderer());
			model.getColumn(2).setCellRenderer(new EtherTxTypeCellRenderer());
			model.getColumn(3).setCellRenderer(new EtherValueCellRenderer(address));
			model.getColumn(4).setCellRenderer(new EtherAddressCellRenderer(address));
		} else if (network.isBurst()) {
			model.getColumn(0).setCellRenderer(new TxIdCellrenderer());
			model.getColumn(1).setCellRenderer(new InstantCellRenderer(network));
			model.getColumn(2).setCellRenderer(new SignumTxTypeCellRenderer());
			model.getColumn(3).setCellRenderer(new SignumValueCellRenderer(network, address));
			model.getColumn(4).setCellRenderer(new SignumAddressCellRenderer(network, address));
		}
	}
}
