package service.framework.exception;

public class ServiceException extends Exception {
	private Exception innerException;
	private String message;
	
	public ServiceException(Exception innerException, String message){
		this.innerException = innerException;
		this.message = message;
	}
	
	public Exception getInnerException() {
		return innerException;
	}
	public void setInnerException(Exception innerException) {
		this.innerException = innerException;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public void printStackTrace() {
		// TODO Auto-generated method stub
		super.printStackTrace();
		if(this.getInnerException() != null){
			this.getInnerException().printStackTrace();
		}
	}
	
	
}
