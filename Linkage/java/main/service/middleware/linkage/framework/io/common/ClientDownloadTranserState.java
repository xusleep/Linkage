package service.middleware.linkage.framework.io.common;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ClientDownloadTranserState implements State {
	
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final FileTransferEntity currentFileInformationEntity;
	private static Logger logger = Logger.getLogger(ClientDownloadTranserState.class);
	
	public ClientDownloadTranserState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, FileTransferEntity currentFileInformationEntity)
	{
		this.fileWorkingChannelStrategy = fileWorkingChannelStrategy;
		this.currentFileInformationEntity = currentFileInformationEntity;
	}

	@Override
	public WorkingChannelOperationResult execute() {
		logger.debug("client download receive the file");
		WorkingChannelOperationResult readResult = this.fileWorkingChannelStrategy.readFile(currentFileInformationEntity);
		this.currentFileInformationEntity.setRequestFileState(FileRequestState.END);
		this.fileWorkingChannelStrategy.setWorkingState(new ClientDownloadEndState(fileWorkingChannelStrategy, currentFileInformationEntity));
		String responseStr  = SerializationUtils.serilizationFileTransferEntity(currentFileInformationEntity);
		this.fileWorkingChannelStrategy.writeMessage(responseStr);
		return readResult;
	}

}
