package service.middleware.linkage.framework.serviceaccess.entity;

import java.util.concurrent.CountDownLatch;

import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.io.WorkingChannelContext;

/**
 * this class stored the request result
 * @author zhonxu
 *
 */
public class RequestResultEntity {
	private String requestID;
	private ResponseEntity responseEntity;
	private CountDownLatch signal = new CountDownLatch(1);
	private boolean isException;
	private ServiceException exception;
	private WorkingChannelContext workingChannel;
	private ServiceInformationEntity serviceInformationEntity;
	
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

	public WorkingChannelContext getWorkingChannel() {
		return workingChannel;
	}

	public void setWorkingChannel(WorkingChannelContext workingChannel) {
		this.workingChannel = workingChannel;
	}

	public ServiceInformationEntity getServiceInformationEntity() {
		return serviceInformationEntity;
	}

	public void setServiceInformationEntity(
			ServiceInformationEntity serviceInformationEntity) {
		this.serviceInformationEntity = serviceInformationEntity;
	}
	
	
}
