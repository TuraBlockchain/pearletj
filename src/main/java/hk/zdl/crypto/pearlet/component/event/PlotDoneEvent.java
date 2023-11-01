package hk.zdl.crypto.pearlet.component.event;

import java.nio.file.Path;

public class PlotDoneEvent {

	public final Path path;

	public PlotDoneEvent(Path path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "PlotDoneEvent [path=" + path + "]";
	}
}
