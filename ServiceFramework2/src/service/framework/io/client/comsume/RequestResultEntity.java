package service.framework.io.client.comsume;

import java.util.concurrent.CountDownLatch;

import service.framework.provide.entity.ResponseEntity;

public class RequestResultEntity {
	private String requestID;
	private ResponseEntity responseEntity;
	private CountDownLatch signal = new CountDownLatch(1);
	
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
	
	
}
