package service.framework.event;

import service.framework.io.common.WorkingChannel;

public class ServiceOnMessageWriteEvent implements ServiceEvent {
	private WorkingChannel workingChannel;
	private String message;
	private String requestID;
	
	public ServiceOnMessageWriteEvent(WorkingChannel workingChannel, String requestID)
	{
		this.workingChannel = workingChannel;
	}

	
	public WorkingChannel getWrokingChannel() {
		return workingChannel;
	}


	public void setWorkingChannel(WorkingChannel socketChannel) {
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
