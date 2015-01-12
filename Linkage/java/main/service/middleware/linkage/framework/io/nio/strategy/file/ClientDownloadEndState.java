package service.middleware.linkage.framework.io.nio.strategy.file;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.WorkingChannelOperationResult;
import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ClientDownloadEndState implements State {
	
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final FileTransferEntity currentFileInformationEntity;
	private static Logger logger = Logger.getLogger(ClientDownloadEndState.class);
	
	public ClientDownloadEndState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, FileTransferEntity currentFileInformationEntity)
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
		FileTransferEntity objFileInformation = SerializationUtils.deserilizationFileTransferEntity(receiveData);
		if(objFileInformation.getRequestFileState() == FileRequestState.ENDOK){
			logger.debug("client download receive end ok confirm from the server, the state will change back to client free state");
			this.fileWorkingChannelStrategy.setWorkingState(new ClientFreeState(fileWorkingChannelStrategy));
			return new WorkingChannelOperationResult(true);
		}
		else if(objFileInformation.getRequestFileState() == FileRequestState.WRONG)
		{
			this.fileWorkingChannelStrategy.setWorkingState(new ClientFreeState(fileWorkingChannelStrategy));
			return readResult;
		}
		return new WorkingChannelOperationResult(true);
	}

}
