package service.middleware.linkage.framework.io.common;

import java.io.File;

import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ClientUploadRequestState implements State {
	
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final String fileUploadPath;
	private final String fileSavePath;
	
	public ClientUploadRequestState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, String fileUploadPath, String fileSavePath)
	{
		this.fileWorkingChannelStrategy = fileWorkingChannelStrategy;
		this.fileUploadPath = fileUploadPath;
		this.fileSavePath = fileSavePath;
	}

	@Override
	public WorkingChannelOperationResult execute() {
		File uploadFile = new File(this.fileUploadPath);
		FileTransferEntity currentFileInformationEntity = new FileTransferEntity(this.fileUploadPath, this.fileSavePath);
		currentFileInformationEntity.setRequestFileState(FileRequestState.UPLOAD);
		String requestData = SerializationUtils.serilizationFileInformationEntity(currentFileInformationEntity);
		this.fileWorkingChannelStrategy.setWorkingState(new ClientUploadAndConfirmFileState(fileWorkingChannelStrategy, currentFileInformationEntity));
		WorkingChannelOperationResult writeResult = this.fileWorkingChannelStrategy.writeMessage(requestData);
		return writeResult;
	}

}
