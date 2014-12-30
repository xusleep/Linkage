package service.middleware.linkage.framework.handlers;

import java.io.IOException;

import service.middleware.linkage.framework.event.ServiceEvent;

public interface Handler {
	public void handleRequest(ServiceEvent event) throws IOException, Exception;
}
