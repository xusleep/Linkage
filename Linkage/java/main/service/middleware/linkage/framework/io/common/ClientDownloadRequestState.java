package service.middleware.linkage.framework.io.common;

import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ClientDownloadRequestState implements State {
	
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final String fileDownloadPath;
	private final String fileSavePath;
	
	public ClientDownloadRequestState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, 
			String fileDownloadPath, String fileSavePath)
	{
		this.fileWorkingChannelStrategy = fileWorkingChannelStrategy;
		this.fileDownloadPath = fileDownloadPath;
		this.fileSavePath = fileSavePath;
	}

	@Override
	public WorkingChannelOperationResult execute() {
		FileRequestEntity currentFileInformationEntity = new FileRequestEntity(this.fileDownloadPath, this.fileSavePath);
		currentFileInformationEntity.setRequestFileState(FileRequestState.DOWNLOAD);
		String requestData = SerializationUtils.serilizationFileInformationEntity(currentFileInformationEntity);
		WorkingChannelOperationResult writeResult = this.fileWorkingChannelStrategy.writeMessage(requestData);
		this.fileWorkingChannelStrategy.setWorkingState(new ClientDownloadAndConfirmState(fileWorkingChannelStrategy, currentFileInformationEntity));
		return writeResult;
	}

}
