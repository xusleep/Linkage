package service.framework.io.fire;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import service.framework.io.event.ServiceEvent;
import service.framework.io.event.ServiceOnAcceptedEvent;
import service.framework.io.handlers.AcceptConnectionHandler;
import service.framework.io.server.WorkerPool;

public class Fires
{	
	public static void fireConnectAccept(ServiceOnAcceptedEvent event) throws Exception {
		AcceptConnectionHandler.getInstance().handleRequest(null, event);
	}	
	
}
