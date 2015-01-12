package service.middleware.linkage.framework.io.nio.strategy.file;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.common.WorkingChannelOperationResult;
import service.middleware.linkage.framework.serialization.SerializationUtils;


public class ServerAcceptRequestState implements State{
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private static Logger logger = Logger.getLogger(ServerAcceptRequestState.class);
	
	public ServerAcceptRequestState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy)
	{
		logger.debug("server ready for accept new request.");
		this.fileWorkingChannelStrategy = fileWorkingChannelStrategy;
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
		if(objFileInformation.getRequestFileState() == FileRequestState.UPLOAD){
			logger.debug("server accept upload request and send upload ok confirm to client.");
			objFileInformation.setRequestFileState(FileRequestState.UPLOADOK);
			this.fileWorkingChannelStrategy.setWorkingState(new ServerUploadFileReceiveState(fileWorkingChannelStrategy, objFileInformation));
			responseData = SerializationUtils.serilizationFileTransferEntity(objFileInformation);
			return this.fileWorkingChannelStrategy.writeMessage(responseData);
		}
		else if(objFileInformation.getRequestFileState() == FileRequestState.DOWNLOAD){
			logger.debug("server accept download request and send transfer request to client.");
			objFileInformation.setRequestFileState(FileRequestState.DOWNLOADTRANSER);
			this.fileWorkingChannelStrategy.setWorkingState(new ServerDownloadConfirmAndTransferState(fileWorkingChannelStrategy, objFileInformation));
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
