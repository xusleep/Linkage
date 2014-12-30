package service.middleware.framework.handlers;

import java.io.IOException;

import service.middleware.framework.event.ServiceEvent;

public interface Handler {
	public void handleRequest(ServiceEvent event) throws IOException, Exception;
}
