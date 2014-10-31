package service.framework.event;

import service.framework.io.common.WorkingChannel;

public class ServiceOnChannelClosedEvent implements ServiceEvent {
	
	private WorkingChannel socketChannel;
	private String message;
	private String requestID;
	
	public ServiceOnChannelClosedEvent(WorkingChannel socketChannel, String requestID)
	{
		this.socketChannel = socketChannel;
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
}
