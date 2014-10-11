package service.framework.io.event;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import service.framework.io.server.Server;

public class ServiceOnConnectedEvent implements ServiceEvent {
	private SelectionKey selectionKey;
	private Server server;
	
	public ServiceOnConnectedEvent(SelectionKey selectionKey, Server client)
	{
		this.selectionKey = selectionKey;
		this.server = client;
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
