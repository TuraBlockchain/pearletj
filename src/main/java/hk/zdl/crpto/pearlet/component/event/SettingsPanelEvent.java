package hk.zdl.crpto.pearlet.component.event;

public class SettingsPanelEvent {

	public static final String NET = "Networks", ACC = "Accounts";
	private final String str;

	public SettingsPanelEvent(String str) {
		this.str = str;
	}

	public String getString() {
		return str;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MyGUIMessageEvent [str=").append(str).append("]");
		return builder.toString();
	}

}
