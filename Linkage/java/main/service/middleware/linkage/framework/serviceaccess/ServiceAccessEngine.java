package service.middleware.linkage.framework.serviceaccess;

import java.util.List;

import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.io.WorkingChannelContext;
import service.middleware.linkage.framework.serviceaccess.entity.RequestEntity;
import service.middleware.linkage.framework.serviceaccess.entity.RequestResultEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ResponseEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;

public interface ServiceAccessEngine {
	
	/**
	 * basic process request
	 * @param objRequestEntity
	 * @param result
	 * @param serviceInformationEntity
	 * @param channelFromCached
	 * @return
	 */
	public RequestResultEntity basicProcessRequest(RequestEntity objRequestEntity, RequestResultEntity result, 
			ServiceInformationEntity serviceInformationEntity, boolean channelFromCached);
	
	/**
	 * create a request entity
	 * @param clientID
	 * @param args
	 * @return
	 */
	public RequestEntity createRequestEntity(String clientID, List<String> args);
	
	/**
	 * close the channel by request result
	 * the client could determine close the channel or not
	 * this method is used along with method prcessRequestPerConnect
	 * don't use it along with method prcessRequest
	 * @param objRequestResultEntity
	 */
	public void closeChannelByRequestResult(RequestResultEntity objRequestResultEntity);
	/**
	 * remove the channel from the cache 
	 * @param requestID
	 */
	public void removeCachedChannel(WorkingChannelContext objWorkingChannel);
	/**
	 * when the response comes, use this method to set it. 
	 * @param objResponseEntity
	 */
	public RequestResultEntity setRequestResult(ResponseEntity objResponseEntity);
	
	/**
	 * clear the result
	 */
	public void clearAllResult(ServiceException exception);
}
