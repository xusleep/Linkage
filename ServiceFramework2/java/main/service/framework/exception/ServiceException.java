package service.framework.exception;

import java.io.PrintStream;

/**
 * this is a service exception defined for this frame work
 * the exception will be wrapped by it.
 * @author zhonxu
 *
 */
public class ServiceException extends Exception {
	/**
	 * this exception will be set when there is excetion happen
	 */
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
		return message + (this.getInnerException() != null ? "\r\ninner message: " + this.getInnerException().getMessage() : "");
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
