package hk.zdl.crypto.pearlet.component.miner;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

public class PlotProgressPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1150055243038748734L;
	public static final String plot_path = "/api/v1/plot";
	private final JButton add_btn = new JButton("Add a Plot");
	private final JTable table = new JTable(6,6);

	private String basePath = "";

	public PlotProgressPanel() {
		super(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Plot", TitledBorder.CENTER, TitledBorder.TOP, MinerGridTitleFont.getFont()));
		add(add_btn,BorderLayout.NORTH);
		add(table,BorderLayout.CENTER);
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

}
