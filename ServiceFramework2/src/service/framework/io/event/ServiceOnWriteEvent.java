package service.framework.io.event;

import java.nio.channels.SelectionKey;

import service.framework.io.server.DefaultServer;
import service.framework.io.server.Server;

public class ServiceOnWriteEvent implements ServiceEvent {
	private SelectionKey selectionKey;
	
	public ServiceOnWriteEvent(SelectionKey selectionKey)
	{
		this.selectionKey = selectionKey;
	}

	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	public void setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}
}
