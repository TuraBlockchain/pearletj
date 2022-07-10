package hk.zdl.crypto.pearlet.misc;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import signumj.entity.response.Asset;

public class SignumAssertListCellRenderer extends DefaultListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4406376446816086685L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Asset a = (Asset) value;
		super.getListCellRendererComponent(list, a.getName(), index, isSelected, cellHasFocus);
		return this;
	}

}
