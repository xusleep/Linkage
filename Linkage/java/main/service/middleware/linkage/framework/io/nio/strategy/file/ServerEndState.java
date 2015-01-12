package service.middleware.linkage.framework.io.nio.strategy.file;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.common.WorkingChannelOperationResult;
import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ServerEndState implements State{
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final FileTransferEntity currentFileInformationEntity;
	private static Logger logger = Logger.getLogger(ServerEndState.class);
	
	public ServerEndState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, FileTransferEntity currentFileInformationEntity)
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
			this.fileWorkingChannelStrategy.setWorkingState(new ServerAcceptRequestState(fileWorkingChannelStrategy));
			return readResult;
		}
		if(messages == null || messages.size() == 0){
			this.fileWorkingChannelStrategy.setWorkingState(new ServerAcceptRequestState(fileWorkingChannelStrategy));
			return new WorkingChannelOperationResult(true);
		}
		String receiveData = messages.get(0);
		String responseData;
		FileTransferEntity objFileInformation = SerializationUtils.deserilizationFileInformationEntity(receiveData);
		if(objFileInformation.getRequestFileState() == FileRequestState.END){
			logger.debug("server receive end request and send end ok confirm to client.");
			objFileInformation.setRequestFileState(FileRequestState.ENDOK);
			this.fileWorkingChannelStrategy.setWorkingState(new ServerAcceptRequestState(fileWorkingChannelStrategy));
			responseData = SerializationUtils.serilizationFileTransferEntity(objFileInformation);
			return this.fileWorkingChannelStrategy.writeMessage(responseData);
		}
		else
		{
			objFileInformation.setRequestFileState(FileRequestState.WRONG);
			this.fileWorkingChannelStrategy.setWorkingState(new ServerAcceptRequestState(fileWorkingChannelStrategy));
			responseData = SerializationUtils.serilizationFileTransferEntity(objFileInformation);
			return this.fileWorkingChannelStrategy.writeMessage(responseData);
		}
	}

}
