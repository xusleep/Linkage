package service.framework.io.common;

import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ResponseEntity;
import service.framework.event.ServiceOnMessageWriteEvent;
import service.framework.exception.ServiceException;

/**
 * hold the object when request a connect,
 * the system will be wrapped by.
 * @author zhonxu
 *
 */
public interface WorkingChannel {
	
	/**
	 * put the request result in the result list
	 * @param requestResultEntity
	 */
	public void offerRequestResult(RequestResultEntity requestResultEntity);
	
	/**
	 * clear the result
	 */
	public void clearAllResult(ServiceException exception);
	
	/**
	 * set the request result
	 * @param requestID
	 * @param strResult
	 */
	public void setRuquestResult(String requestID, String strResult);
	
	/**
	 * set the request result
	 * @param requestID
	 * @param strResult
	 */
	public void setExceptionRuquestResult(String requestID, ServiceException serviceException);
	
	/**
	 * when the response comes, use this method to set it. 
	 * @param objResponseEntity
	 */
	public RequestResultEntity setRequestResult(ResponseEntity objResponseEntity);
	
	/**
	 * get the current worker for this channel
	 * @return
	 */
	public Worker getWorker();
	
	/**
	 * offer the write message into the writting queue
	 * @param serviceOnMessageWriteEvent
	 */
	public void offerWriterQueue(ServiceOnMessageWriteEvent serviceOnMessageWriteEvent);
}
