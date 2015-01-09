package service.middleware.linkage.framework.io.common;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.framework.serialization.SerializationUtils;


public class ServerAcceptRequestState implements State{
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	
	public ServerAcceptRequestState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy)
	{
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
		FileRequestEntity objFileInformation = SerializationUtils.deserilizationFileInformationEntity(receiveData);
		if(objFileInformation.getRequestFileState() == FileRequestState.UPLOAD){
			objFileInformation.setRequestFileState(FileRequestState.UPLOADOK);
			this.fileWorkingChannelStrategy.setWorkingState(new ServerUploadFileReceiveState(fileWorkingChannelStrategy, objFileInformation));
			responseData = SerializationUtils.serilizationFileInformationEntity(objFileInformation);
			return this.fileWorkingChannelStrategy.writeMessage(responseData);
		}
		else if(objFileInformation.getRequestFileState() == FileRequestState.DOWNLOAD){
			// set the file here
			File sentFile = new File(objFileInformation.getFileGetPath());
			objFileInformation.setFileName(sentFile.getName());
			objFileInformation.setFileSize(sentFile.length());
			objFileInformation.setRequestFileState(FileRequestState.DOWNLOADTRANSER);
			this.fileWorkingChannelStrategy.setWorkingState(new ServerDownloadConfirmAndTransferState(fileWorkingChannelStrategy, objFileInformation));
			responseData = SerializationUtils.serilizationFileInformationEntity(objFileInformation);
			return this.fileWorkingChannelStrategy.writeMessage(responseData);
		}
		else
		{
			objFileInformation.setRequestFileState(FileRequestState.WRONG);
			this.fileWorkingChannelStrategy.setWorkingState(new ServerAcceptRequestState(fileWorkingChannelStrategy));
			responseData = SerializationUtils.serilizationFileInformationEntity(objFileInformation);
			return this.fileWorkingChannelStrategy.writeMessage(responseData);
		}
	}

}
