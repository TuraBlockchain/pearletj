package hk.zdl.crypto.pearlet.ds;

public class CryptoNetwork {

	public enum Type {
		BURST, ROTURA, SIGNUM, WEB3J;
	}

	private int id;
	private Type type;
	private String name, url;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getName() {
		return name==null?"":name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isBurst() {
		return type == Type.BURST;
	}

	public boolean isWeb3J() {
		return type == Type.WEB3J;
	}

	@Override
	public String toString() {
		return getName();
	}
}
