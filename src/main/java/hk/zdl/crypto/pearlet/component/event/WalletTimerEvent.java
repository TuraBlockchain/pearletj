package hk.zdl.crypto.pearlet.component.event;

import java.util.Objects;

public class WalletTimerEvent {

	public final int value, total;

	public WalletTimerEvent(int value, int total) {
		this.value = value;
		this.total = total;
	}

	@Override
	public int hashCode() {
		return Objects.hash(total, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WalletTimerEvent other = (WalletTimerEvent) obj;
		return total == other.total && value == other.value;
	}

	@Override
	public String toString() {
		return "WalletTimerEvent [value=" + value + ", total=" + total + "]";
	}
}
