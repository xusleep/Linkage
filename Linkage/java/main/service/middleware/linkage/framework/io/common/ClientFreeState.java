package service.middleware.linkage.framework.io.common;

public class ClientFreeState implements State {
	private final NIOFileWorkingChannelStrategy fileWorkingChannelStrategy;
	
	public ClientFreeState(NIOFileWorkingChannelStrategy fileWorkingChannelStrategy)
	{
		this.fileWorkingChannelStrategy = fileWorkingChannelStrategy;
	}
	
	@Override
	public WorkingChannelOperationResult execute() {
		return this.fileWorkingChannelStrategy.readAndDrop();
	}

}
