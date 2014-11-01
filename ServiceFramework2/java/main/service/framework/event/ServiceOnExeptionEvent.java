package service.framework.event;

import service.framework.exception.ServiceException;
import service.framework.io.common.WorkingChannel;

public class ServiceOnExeptionEvent implements ServiceEvent {
	
	private WorkingChannel socketChannel;
	private ServiceException exceptionHappen;
	private String requestID;
	
	public ServiceOnExeptionEvent(WorkingChannel socketChannel, String requestID, ServiceException exceptionHappen)
	{
		this.socketChannel = socketChannel;
		this.requestID = requestID;
		this.exceptionHappen = exceptionHappen;
	}

	public String getRequestID() {
		return requestID;
	}

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}
	
	public WorkingChannel getSocketChannel() {
		return socketChannel;
	}


	public void setSocketChannel(WorkingChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public ServiceException getExceptionHappen() {
		return exceptionHappen;
	}

	public void setExceptionHappen(ServiceException exceptionHappen) {
		this.exceptionHappen = exceptionHappen;
	}
}
