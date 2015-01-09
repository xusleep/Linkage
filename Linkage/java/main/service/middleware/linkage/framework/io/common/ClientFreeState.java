package service.middleware.linkage.framework.io.common;

public class ClientFreeState implements State {
	
	public ClientFreeState()
	{
	}
	
	@Override
	public WorkingChannelOperationResult execute() {
		// TODO Auto-generated method stub
		return new WorkingChannelOperationResult(true);
	}

}
