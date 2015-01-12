package service.middleware.linkage.framework.io.nio.strategy.file;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.WorkingChannelOperationResult;

public class ServerUploadFileReceiveState implements State{
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final FileTransferEntity currentFileInformationEntity;
	private static Logger logger = Logger.getLogger(ServerUploadFileReceiveState.class);
	
	public ServerUploadFileReceiveState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, FileTransferEntity currentFileInformationEntity)
	{
		this.fileWorkingChannelStrategy = fileWorkingChannelStrategy;
		this.currentFileInformationEntity = currentFileInformationEntity;
	}
	@Override
	public WorkingChannelOperationResult execute() {
		logger.debug("server upload, receive the file.");
		WorkingChannelOperationResult readResult = this.fileWorkingChannelStrategy.readFile(currentFileInformationEntity);
		this.fileWorkingChannelStrategy.setWorkingState(new ServerAcceptRequestState(this.fileWorkingChannelStrategy));
		return readResult;
	}

}
