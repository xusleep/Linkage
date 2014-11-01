package service.framework.event;

import service.framework.io.common.WorkingChannel;

public class ServiceOnMessageWriteEvent implements ServiceEvent {
	private WorkingChannel socketChannel;
	private String message;
	private String requestID;
	
	public ServiceOnMessageWriteEvent(WorkingChannel socketChannel, String requestID)
	{
		this.socketChannel = socketChannel;
	}

	
	public WorkingChannel getSocketChannel() {
		return socketChannel;
	}


	public void setSocketChannel(WorkingChannel socketChannel) {
		this.socketChannel = socketChannel;
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