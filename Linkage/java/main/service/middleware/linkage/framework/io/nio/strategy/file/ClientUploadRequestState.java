package service.middleware.linkage.framework.io.nio.strategy.file;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.common.WorkingChannelOperationResult;
import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ClientUploadRequestState implements State {
	
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final String fileUploadPath;
	private final String fileSavePath;
	private static Logger logger = Logger.getLogger(ClientUploadRequestState.class);
	
	public ClientUploadRequestState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, String fileUploadPath, String fileSavePath)
	{
		logger.debug("client upload is starting ...");
		this.fileWorkingChannelStrategy = fileWorkingChannelStrategy;
		this.fileUploadPath = fileUploadPath;
		this.fileSavePath = fileSavePath;
	}

	@Override
	public WorkingChannelOperationResult execute() {
		logger.debug("client upload send upload request.");
		FileTransferEntity currentFileInformationEntity = new FileTransferEntity(this.fileUploadPath, this.fileSavePath);
		currentFileInformationEntity.setRequestFileState(FileRequestState.UPLOAD);
		String requestData = SerializationUtils.serilizationFileTransferEntity(currentFileInformationEntity);
		this.fileWorkingChannelStrategy.setWorkingState(new ClientUploadAndConfirmFileState(fileWorkingChannelStrategy, currentFileInformationEntity));
		WorkingChannelOperationResult writeResult = this.fileWorkingChannelStrategy.writeMessage(requestData);
		return writeResult;
	}

}
