package hk.zdl.crpto.pearlet.component.dashboard;

import hk.zdl.crpto.pearlet.util.CrptoNetworks;

public class DashboardTxProc {

	private final DashboardTxTableModel table_model;

	public DashboardTxProc(DashboardTxTableModel table_model) {
		this.table_model = table_model;
	}

	public void update(CrptoNetworks network, String address) throws Exception{
		switch(network) {
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
}
