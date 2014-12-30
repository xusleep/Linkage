package service.middleware.linkage.framework.event;

import java.nio.channels.SelectionKey;

import service.middleware.linkage.framework.io.server.Server;

/**
 * this event will be triggered when there is one connection 
 * accepted.
 * @author zhonxu
 *
 */
public class ServiceOnAcceptedEvent implements ServiceEvent {
	private SelectionKey selectionKey;
	private Server server;
	
	public ServiceOnAcceptedEvent(SelectionKey selectionKey, Server server)
	{
		this.selectionKey = selectionKey;
		this.server = server;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	public void setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}
}
