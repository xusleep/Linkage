package service.framework.io.event;

import service.framework.io.server.WorkingChannel;

public class ServiceOnMessageReceiveEvent implements ServiceEvent {
	private WorkingChannel socketChannel;
	private String message;
	
	public ServiceOnMessageReceiveEvent(WorkingChannel socketChannel)
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
}
