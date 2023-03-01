package hk.zdl.crypto.pearlet.ds;

import java.util.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hash(id, name, type, url);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CryptoNetwork other = (CryptoNetwork) obj;
		return id == other.id && Objects.equals(name, other.name) && type == other.type && Objects.equals(url, other.url);
	}
}
