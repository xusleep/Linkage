package service.middleware.linkage.framework.serviceaccess;

import java.util.List;

import service.middleware.linkage.framework.io.WorkingChannelContext;
import service.middleware.linkage.framework.serviceaccess.entity.RequestResultEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;

public interface ServiceAccess {
	
	/**
	 * request directly using the service information entity
	 * @param clientID
	 * @param args
	 * @param serviceInformationEntity
	 * @return
	 */
	public RequestResultEntity requestService(String clientID, List<String> args, ServiceInformationEntity serviceInformationEntity);
	
	/**
	 * request directly using the service information entity
	 * @param clientID
	 * @param args
	 * @param serviceInformationEntity
	 * @param channelFromCached
	 * @return
	 */
	public RequestResultEntity requestService(String clientID, List<String> args, ServiceInformationEntity serviceInformationEntity, boolean channelFromCached);
	
	/**
	 * send the request from the client to request a service
	 * this request will not reuse the cache connect
	 * use this method with closeChannelByRequestResult together
	 * @param clientID the id set in property
	 * @param args  the arguments for the service
	 * @return
	 * @throws Exception
	 */
	public RequestResultEntity requestServicePerConnect(String clientID, List<String> args, ServiceInformationEntity serviceInformationEntity);

	/**
	 * send the request from the client to request a service synchronized
	 * this request will not reuse the channel
	 * this method is a synchronized method, the result will return once the method return
	 * @param clientID the id set in property
	 * @param args  the arguments for the service
	 * @return
	 * @throws Exception
	 */
	public RequestResultEntity requestServicePerConnectSync(String clientID, List<String> args, ServiceInformationEntity serviceInformationEntity);
	
	/**
	 * close the channel by request result
	 * the client could determine close the channel or not
	 * this method is used along with method requestServicePerConnectSync
	 * don't use it along with method prcessRequest
	 * @param objRequestResultEntity
	 */
	public void closeChannelByRequestResult(RequestResultEntity objRequestResultEntity);
	
	/**
	 * remove the channel from the cache 
	 * @param requestID
	 */
	public void removeCachedChannel(WorkingChannelContext objWorkingChannel);
	
	public ServiceAccessEngine getServiceAccessEngine(); 
}
