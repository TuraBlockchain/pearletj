package hk.zdl.crypto.pearlet.component.event;

public class SettingsPanelEvent {

	private final int index;

	public SettingsPanelEvent(int index) {
		super();
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "SettingsPanelEvent [index=" + index + "]";
	}

}
