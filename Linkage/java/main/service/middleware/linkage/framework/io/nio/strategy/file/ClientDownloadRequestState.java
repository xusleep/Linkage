package service.middleware.linkage.framework.io.nio.strategy.file;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.common.WorkingChannelOperationResult;
import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ClientDownloadRequestState implements State {
	
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final String fileDownloadPath;
	private final String fileSavePath;
	private static Logger logger = Logger.getLogger(ClientDownloadRequestState.class);
	
	public ClientDownloadRequestState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, 
			String fileDownloadPath, String fileSavePath)
	{
		logger.debug("client download request is starting...");
		this.fileWorkingChannelStrategy = fileWorkingChannelStrategy;
		this.fileDownloadPath = fileDownloadPath;
		this.fileSavePath = fileSavePath;
	}

	@Override
	public WorkingChannelOperationResult execute() {
		logger.debug("client download send download request to server.");
		FileTransferEntity currentFileInformationEntity = new FileTransferEntity(this.fileDownloadPath, this.fileSavePath);
		currentFileInformationEntity.setRequestFileState(FileRequestState.DOWNLOAD);
		String requestData = SerializationUtils.serilizationFileTransferEntity(currentFileInformationEntity);
		WorkingChannelOperationResult writeResult = this.fileWorkingChannelStrategy.writeMessage(requestData);
		this.fileWorkingChannelStrategy.setWorkingState(new ClientDownloadAndConfirmState(fileWorkingChannelStrategy, currentFileInformationEntity));
		return writeResult;
	}

}
