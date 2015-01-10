package service.middleware.linkage.framework.io.common;

import org.apache.log4j.Logger;

public class ClientFreeState implements State {
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	private static Logger logger = Logger.getLogger(ClientFreeState.class);
	
	public ClientFreeState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy)
	{
		this.fileWorkingChannelStrategy = fileWorkingChannelStrategy;
		logger.debug("client release the request restrain.");
		this.fileWorkingChannelStrategy.getRequestRestrain().release();
	}
	
	@Override
	public WorkingChannelOperationResult execute() {
		return new WorkingChannelOperationResult(true);
	}

}
