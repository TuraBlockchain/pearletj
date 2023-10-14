package hk.zdl.crypto.pearlet.ui;

import java.util.List;

import org.jdesktop.swingx.combobox.ListComboBoxModel;

public class MyListComboBoxModel<E> extends ListComboBoxModel<E> {

	private static final long serialVersionUID = 1988137018627771585L;

	public MyListComboBoxModel(List<E> list) {
		super(list);
	}
	
	public void setElementAt(int index,E e) {
		data.set(index, e);
		fireContentsChanged(this, index, index);
	}

}
