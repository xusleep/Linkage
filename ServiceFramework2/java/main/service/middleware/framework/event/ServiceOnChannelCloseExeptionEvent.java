package service.middleware.framework.event;

import service.middleware.framework.exception.ServiceException;
import service.middleware.framework.io.common.WorkingChannel;

/**
 * this event will be triggered when there is a channel close exception happen
 * @author zhonxu
 *
 */
public class ServiceOnChannelCloseExeptionEvent implements ServiceEvent {
	
	private WorkingChannel workingChannel;
	private ServiceException exceptionHappen;
	private String requestID;
	
	public ServiceOnChannelCloseExeptionEvent(WorkingChannel socketChannel, String requestID, ServiceException exceptionHappen)
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
	
	public WorkingChannel getWorkingChannel() {
		return workingChannel;
	}

	public void setWorkingChannel(WorkingChannel socketChannel) {
		this.workingChannel = socketChannel;
	}

	public ServiceException getExceptionHappen() {
		return exceptionHappen;
	}

	public void setExceptionHappen(ServiceException exceptionHappen) {
		this.exceptionHappen = exceptionHappen;
	}
}
