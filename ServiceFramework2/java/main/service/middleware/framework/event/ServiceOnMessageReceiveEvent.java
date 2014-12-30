package service.middleware.framework.event;

import service.middleware.framework.io.common.WorkingChannel;

/**
 * this event will be triggered when there is a message arrived
 * @author zhonxu
 *
 */
public class ServiceOnMessageReceiveEvent implements ServiceEvent {
	private WorkingChannel workingChannel;
	private String message;
	
	public ServiceOnMessageReceiveEvent(WorkingChannel socketChannel)
	{
		this.workingChannel = socketChannel;
	}

	public WorkingChannel getWorkingChannel() {
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
}
