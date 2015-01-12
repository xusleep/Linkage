package service.middleware.linkage.framework.io;

public class WorkingChannelOperationResult {
	private boolean isSuccess;
	
	public WorkingChannelOperationResult(boolean isSuccess){
		this.isSuccess = isSuccess;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	
}
