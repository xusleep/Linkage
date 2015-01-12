package service.middleware.linkage.framework.handlers;

import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.io.WorkingChannelContext;

/**
 * this event will be triggered when there is a channel close exception happen
 * @author zhonxu
 *
 */
public class ServiceExeptionEvent implements ServiceEvent {
	
	private WorkingChannelContext workingChannel;
	private ServiceException exceptionHappen;
	private String requestID;
	
	public ServiceExeptionEvent(WorkingChannelContext socketChannel, String requestID, ServiceException exceptionHappen)
	{
		this.workingChannel = socketChannel;
		this.requestID = requestID;
		this.exceptionHappen = exceptionHappen;
	}

	public String getRequestID() {
		return requestID;
	}

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}
	
	public WorkingChannelContext getWorkingChannel() {
		return workingChannel;
	}

	public void setWorkingChannel(WorkingChannelContext socketChannel) {
		this.workingChannel = socketChannel;
	}

	public ServiceException getExceptionHappen() {
		return exceptionHappen;
	}

	public void setExceptionHappen(ServiceException exceptionHappen) {
		this.exceptionHappen = exceptionHappen;
	}
}
