package service.middleware.linkage.center.comsume;

import java.util.List;

import service.middleware.linkage.framework.common.entity.RequestResultEntity;

public interface RouteConsume {
	/**
	 * request directly using the service information entity
	 * @param clientID
	 * @param args
	 * @return
	 */
	public RequestResultEntity requestService(String clientID, List<String> args);
	
	/**
	 * request directly using the service information entity
	 * @param clientID
	 * @param args
	 * @param channelFromCached
	 * @return
	 */
	public RequestResultEntity requestService(String clientID, List<String> args, boolean channelFromCached);
	
	/**
	 * send the request from the client to request a service
	 * this request will not reuse the cache connect
	 * use this method with closeChannelByRequestResult
	 * @param clientID the id set in property
	 * @param args  the arguments for the service
	 * @return
	 * @throws Exception
	 */
	public RequestResultEntity requestServicePerConnect(String clientID, List<String> args);

	/**
	 * send the request from the client to request a service synchronized
	 * this request will not reuse the channel
	 * this method is a synchronized method, the result will return once the method return
	 * @param clientID the id set in property
	 * @param args  the arguments for the service
	 * @return
	 * @throws Exception
	 */
	public RequestResultEntity requestServicePerConnectSync(String clientID, List<String> args);
	
}
