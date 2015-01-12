package service.middleware.linkage.framework.io.nio.strategy.mixed.events;

import service.middleware.linkage.framework.handlers.ServiceEvent;
import service.middleware.linkage.framework.io.common.WorkingChannelContext;

public class ServiceOnMessageDataReceivedEvent implements ServiceEvent {
	private byte[] messageData;
	private WorkingChannelContext workingChannel;
	
	public ServiceOnMessageDataReceivedEvent(WorkingChannelContext workingChannel, byte[] messageData){
		this.workingChannel = workingChannel;
		this.messageData = messageData;
	} 

	public byte[] getMessageData() {
		return messageData;
	}

	public void setMessageData(byte[] data) {
		this.messageData = data;
	}

	public WorkingChannelContext getWorkingChannel() {
		return workingChannel;
	}

	public void setWorkingChannel(WorkingChannelContext workingChannel) {
		this.workingChannel = workingChannel;
	}
	
	
}
