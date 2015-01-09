package service.middleware.linkage.framework.io.common;

import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ClientUploadAndConfirmFileState implements State {
	
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final FileTransferEntity currentFileInformationEntity;
	
	public ClientUploadAndConfirmFileState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, FileTransferEntity currentFileInformationEntity)
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
			this.fileWorkingChannelStrategy.setWorkingState(new ClientFreeState(fileWorkingChannelStrategy));
			return readResult;
		}
		String receiveData = messages.get(0);
		FileTransferEntity objFileInformation = SerializationUtils.deserilizationFileInformationEntity(receiveData);
		if(objFileInformation.getRequestFileState() == FileRequestState.UPLOADOK){
			WorkingChannelOperationResult writeResult = this.fileWorkingChannelStrategy.writeFile(currentFileInformationEntity);
			this.fileWorkingChannelStrategy.setWorkingState(new ClientFreeState(fileWorkingChannelStrategy));
			return writeResult;
		}
		else if(objFileInformation.getRequestFileState() == FileRequestState.WRONG)
		{
			this.fileWorkingChannelStrategy.setWorkingState(new ClientFreeState(fileWorkingChannelStrategy));
			return readResult;
		}
		return new WorkingChannelOperationResult(true);
	}

}
