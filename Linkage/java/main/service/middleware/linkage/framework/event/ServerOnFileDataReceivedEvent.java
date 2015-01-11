package service.middleware.linkage.framework.event;

import service.middleware.linkage.framework.io.common.WorkingChannelContext;

public class ServerOnFileDataReceivedEvent implements ServiceEvent {
	private WorkingChannelContext workingChannel;
	private long fileID;
	
	public ServerOnFileDataReceivedEvent(WorkingChannelContext workingChannel, long fileID){
		this.workingChannel = workingChannel;
		this.fileID = fileID;
	} 
	public WorkingChannelContext getWorkingChannel() {
		return workingChannel;
	}
	public void setWorkingChannel(WorkingChannelContext workingChannel) {
		this.workingChannel = workingChannel;
	}
	public long getFileID() {
		return fileID;
	}
	public void setFileID(long fileID) {
		this.fileID = fileID;
	}
}
