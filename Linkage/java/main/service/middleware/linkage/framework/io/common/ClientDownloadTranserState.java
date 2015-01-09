package service.middleware.linkage.framework.io.common;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.framework.serialization.SerializationUtils;

public class ClientDownloadTranserState implements State {
	
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private final FileRequestEntity currentFileInformationEntity;
	
	public ClientDownloadTranserState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy, FileRequestEntity currentFileInformationEntity)
	{
		this.fileWorkingChannelStrategy = fileWorkingChannelStrategy;
		this.currentFileInformationEntity = currentFileInformationEntity;
	}

	@Override
	public WorkingChannelOperationResult execute() {
		WorkingChannelOperationResult readResult = this.fileWorkingChannelStrategy.readFile(currentFileInformationEntity);
		this.fileWorkingChannelStrategy.setWorkingState(new ClientFreeState());
		return readResult;
	}

}
