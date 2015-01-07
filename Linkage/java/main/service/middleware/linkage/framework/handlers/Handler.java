package service.middleware.linkage.framework.handlers;

import java.io.IOException;

import service.middleware.linkage.framework.event.ServiceEvent;

/**
 * the abtract handler for the handlers
 * it's a double link list
 * @author zhonxu
 *
 */
public abstract class Handler {
	private Handler next;
	private Handler previous;
	
	public abstract void handleRequest(ServiceEvent event) throws IOException, Exception;
	
	public Handler getNext() {
		return next;
	}
	public void setNext(Handler next) {
		this.next = next;
	}
	public Handler getPrevious() {
		return previous;
	}
	public void setPrevious(Handler previous) {
		this.previous = previous;
	}
}
