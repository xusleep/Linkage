package service.middleware.linkage.framework.io.common;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ClientDownloadAndConfirmState implements State {
	
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final FileRequestEntity currentFileInformationEntity;
	
	public ClientDownloadAndConfirmState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, FileRequestEntity currentFileInformationEntity)
	{
		this.fileWorkingChannelStrategy = fileWorkingChannelStrategy;
		this.currentFileInformationEntity = currentFileInformationEntity;
	}

	@Override
	public WorkingChannelOperationResult execute() {
		List<String> messages = new LinkedList<String>();
		WorkingChannelOperationResult readResult = this.fileWorkingChannelStrategy.readMessages(messages);
		if(!readResult.isSuccess())
		{
			this.fileWorkingChannelStrategy.setWorkingState(new ClientFreeState());
			return readResult;
		}
		String receiveData = messages.get(0);
		FileRequestEntity objFileInformation = SerializationUtils.deserilizationFileInformationEntity(receiveData);
		if(objFileInformation.getRequestFileState() == FileRequestState.DOWNLOADTRANSER){
			this.currentFileInformationEntity.setRequestFileState(FileRequestState.DOWNLOADTRANSEROK);
			this.currentFileInformationEntity.setFileName(objFileInformation.getFileName());
			this.currentFileInformationEntity.setFileSize(objFileInformation.getFileSize());
			String requestData = SerializationUtils.serilizationFileInformationEntity(currentFileInformationEntity);
			this.fileWorkingChannelStrategy.setWorkingState(new ClientDownloadTranserState(fileWorkingChannelStrategy, currentFileInformationEntity));
			WorkingChannelOperationResult writeResult = this.fileWorkingChannelStrategy.writeMessage(requestData);
		}
		else if(objFileInformation.getRequestFileState() == FileRequestState.WRONG)
		{
			this.fileWorkingChannelStrategy.setWorkingState(new ClientFreeState());
			return readResult;
		}
		return new WorkingChannelOperationResult(true);
	}

}
