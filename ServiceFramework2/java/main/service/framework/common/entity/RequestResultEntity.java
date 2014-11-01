package service.framework.common.entity;

import java.util.concurrent.CountDownLatch;

import service.framework.exception.ServiceException;

public class RequestResultEntity {
	private String requestID;
	private ResponseEntity responseEntity;
	private CountDownLatch signal = new CountDownLatch(1);
	private boolean isException;
	private ServiceException exception;
	private boolean isCloseingAfterRequest;
	
	public RequestResultEntity(){
	}

	public String getRequestID() {
		return requestID;
	}

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	public ResponseEntity getResponseEntity() {
		try {
			signal.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseEntity;
	}

	public void setResponseEntity(ResponseEntity responseEntity) {
		this.responseEntity = responseEntity;
		signal.countDown();
	}

	public boolean isException() {
		try {
			signal.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isException;
	}

	public void setException(boolean isException) {
		this.isException = isException;
	}

	public ServiceException getException() {
		return exception;
	}

	public void setException(ServiceException exception) {
		this.exception = exception;
	}

	public boolean isCloseingAfterRequest() {
		return isCloseingAfterRequest;
	}

	public void setCloseingAfterRequest(boolean isCloseingAfterRequest) {
		this.isCloseingAfterRequest = isCloseingAfterRequest;
	}
	
	
}
