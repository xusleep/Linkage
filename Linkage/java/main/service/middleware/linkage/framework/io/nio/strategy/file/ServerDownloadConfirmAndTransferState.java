package service.middleware.linkage.framework.io.nio.strategy.file;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.WorkingChannelOperationResult;
import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ServerDownloadConfirmAndTransferState implements State{
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final FileTransferEntity currentFileInformationEntity;
	private static Logger logger = Logger.getLogger(ServerDownloadConfirmAndTransferState.class);
	
	public ServerDownloadConfirmAndTransferState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, FileTransferEntity currentFileInformationEntity)
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
		FileTransferEntity objFileInformation = SerializationUtils.deserilizationFileTransferEntity(receiveData);
		if(objFileInformation.getRequestFileState() == FileRequestState.DOWNLOADTRANSEROK){
			logger.debug("server download receive the transfer request confirm and send the file to client");
			WorkingChannelOperationResult writeResult = this.fileWorkingChannelStrategy.writeFile(currentFileInformationEntity);
			this.fileWorkingChannelStrategy.setWorkingState(new ServerEndState(this.fileWorkingChannelStrategy, currentFileInformationEntity));
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
