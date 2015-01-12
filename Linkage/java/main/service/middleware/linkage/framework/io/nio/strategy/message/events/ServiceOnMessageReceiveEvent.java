package service.middleware.linkage.framework.io.nio.strategy.message.events;

import service.middleware.linkage.framework.handlers.ServiceEvent;
import service.middleware.linkage.framework.io.WorkingChannelContext;

/**
 * this event will be triggered when there is a message arrived
 * @author zhonxu
 *
 */
public class ServiceOnMessageReceiveEvent implements ServiceEvent {
	private WorkingChannelContext workingChannel;
	private String message;
	
	public ServiceOnMessageReceiveEvent(WorkingChannelContext socketChannel)
	{
		this.workingChannel = socketChannel;
	}

	public WorkingChannelContext getWorkingChannel() {
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
}
