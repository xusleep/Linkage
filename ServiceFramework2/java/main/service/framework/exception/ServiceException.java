package service.framework.exception;

import java.io.PrintStream;

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
		printStackTrace(System.err);
	}

	@Override
	public void printStackTrace(PrintStream s) {
		// TODO Auto-generated method stub
		super.printStackTrace(s);
		if(this.getInnerException() != null){
			this.getInnerException().printStackTrace();
		}
	}
}
