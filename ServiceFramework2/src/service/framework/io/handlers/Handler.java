package service.framework.io.handlers;

import java.io.IOException;

import service.framework.io.context.ServiceContext;
import service.framework.io.event.ServiceEvent;

public interface Handler {
	public void handleRequest(ServiceContext context, ServiceEvent event) throws IOException, Exception;
}
