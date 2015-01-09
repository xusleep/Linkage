package service.middleware.linkage.framework.io.common;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ServerDownloadConfirmAndTransferState implements State{
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final FileRequestEntity currentFileInformationEntity;
	
	public ServerDownloadConfirmAndTransferState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, FileRequestEntity currentFileInformationEntity)
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
			this.fileWorkingChannelStrategy.setWorkingState(new ServerAcceptRequestState(this.fileWorkingChannelStrategy));
			return readResult;
		}
		String receiveData = messages.get(0);
		FileRequestEntity objFileInformation = SerializationUtils.deserilizationFileInformationEntity(receiveData);
		if(objFileInformation.getRequestFileState() == FileRequestState.DOWNLOADTRANSEROK){
			WorkingChannelOperationResult writeResult = this.fileWorkingChannelStrategy.writeFile(currentFileInformationEntity);
			this.fileWorkingChannelStrategy.setWorkingState(new ServerAcceptRequestState(this.fileWorkingChannelStrategy));
			return writeResult;
		}
		else if(objFileInformation.getRequestFileState() == FileRequestState.WRONG)
		{
			this.fileWorkingChannelStrategy.setWorkingState(new ServerAcceptRequestState(this.fileWorkingChannelStrategy));
			return readResult;
		}
		return new WorkingChannelOperationResult(true);
	}

}
