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
		return name;
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

	@Override
	public String toString() {
		return "CryptoNetwork [id=" + id + ", type=" + type + ", name=" + name + ", url=" + url + "]";
	}
}
