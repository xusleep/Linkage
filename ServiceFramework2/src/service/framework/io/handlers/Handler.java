package service.framework.io.handlers;

import java.io.IOException;

import service.framework.io.event.ServiceEvent;

public interface Handler {
	public void handleRequest(ServiceEvent event) throws IOException, Exception;
}
