package service.middleware.linkage.framework.io.common;

import java.io.File;

public class ServerUploadFileReceiveState implements State{
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final FileTransferEntity currentFileInformationEntity;
	
	public ServerUploadFileReceiveState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, FileTransferEntity currentFileInformationEntity)
	{
		this.fileWorkingChannelStrategy = fileWorkingChannelStrategy;
		this.currentFileInformationEntity = currentFileInformationEntity;
	}
	@Override
	public WorkingChannelOperationResult execute() {
		WorkingChannelOperationResult readResult = this.fileWorkingChannelStrategy.readFile(currentFileInformationEntity);
		this.fileWorkingChannelStrategy.setWorkingState(new ServerAcceptRequestState(this.fileWorkingChannelStrategy));
		return readResult;
	}

}
