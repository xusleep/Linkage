package service.middleware.linkage.framework.event;

import service.middleware.linkage.framework.io.common.WorkingChannelContext;

/**
 * this event will be triggered when there is a message need to be write in the channel
 * @author zhonxu
 *
 */
public class ServiceOnMessageWriteEvent implements ServiceEvent {
	private WorkingChannelContext workingChannel;
	private String message;
	private String requestID;
	
	public ServiceOnMessageWriteEvent(WorkingChannelContext workingChannel, String requestID)
	{
		this.workingChannel = workingChannel;
	}

	
	public WorkingChannelContext getWrokingChannel() {
		return workingChannel;
	}


	public void setWorkingChannel(WorkingChannelContext socketChannel) {
		this.workingChannel = socketChannel;
	}


	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getRequestID() {
		return requestID;
	}


	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}
}
