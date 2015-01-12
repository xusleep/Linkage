package service.middleware.linkage.framework.io.nio.strategy.file;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.common.WorkingChannelOperationResult;
import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ClientDownloadAndConfirmState implements State {
	
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final FileTransferEntity currentFileInformationEntity;
	private static Logger logger = Logger.getLogger(ClientDownloadAndConfirmState.class);
	
	public ClientDownloadAndConfirmState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, FileTransferEntity currentFileInformationEntity)
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
		if(objFileInformation.getRequestFileState() == FileRequestState.DOWNLOADTRANSER){
			logger.debug("client download receive the transfer request from the server");
			this.currentFileInformationEntity.setRequestFileState(FileRequestState.DOWNLOADTRANSEROK);
			String requestData = SerializationUtils.serilizationFileTransferEntity(currentFileInformationEntity);
			this.fileWorkingChannelStrategy.setWorkingState(new ClientDownloadTranserState(fileWorkingChannelStrategy, currentFileInformationEntity));
			WorkingChannelOperationResult writeResult = this.fileWorkingChannelStrategy.writeMessage(requestData);
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
