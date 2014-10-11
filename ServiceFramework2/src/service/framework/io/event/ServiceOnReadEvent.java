package service.framework.io.event;

import java.nio.channels.SelectionKey;

import service.framework.io.server.Server;
import service.framework.io.server.Worker;

public class ServiceOnReadEvent implements ServiceEvent {
	private SelectionKey selectionKey;
	private Worker server;
	
	public ServiceOnReadEvent(SelectionKey selectionKey, Worker server)
	{
		this.selectionKey = selectionKey;
		this.server = server;
	}

	public Worker getServer() {
		return server;
	}

	public void setServer(Worker server) {
		this.server = server;
	}

	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	public void setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}
}
