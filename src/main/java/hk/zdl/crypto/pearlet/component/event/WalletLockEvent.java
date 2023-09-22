package hk.zdl.crypto.pearlet.component.event;


public class WalletLockEvent {

	public enum Type {
		LOCK,UNLOCK
	}

	public final Type type;

	public WalletLockEvent(Type type) {
		super();
		this.type = type;
	}

	@Override
	public String toString() {
		return "WalletLockEvent [type=" + type + "]";
	}
}
