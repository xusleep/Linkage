package service.framework.comsume;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import service.framework.common.SerializeUtils;
import service.framework.common.StringUtils;
import service.framework.common.entity.RequestEntity;
import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ServiceInformationEntity;
import service.framework.event.ServiceOnMessageWriteEvent;
import service.framework.exception.ServiceException;
import service.framework.io.common.WorkerPool;
import service.framework.io.common.WorkingChannel;
import service.framework.properties.ClientPropertyEntity;
import service.framework.properties.WorkingClientPropertyEntity;
import service.framework.route.AbstractRoute;

/**
 * this class is an access point from the client
 * the client will use this class to call the service  
 * @author zhonxu
 *
 */
public class ConsumerBean {
	/**
	 * used to cached the {@link WorkingChannel} object
	 */
	private final HashMap<String, WorkingChannel> workingChannelCacheList = new HashMap<String, WorkingChannel>(16);
	private AtomicLong idGenerator = new AtomicLong(0);
	private final WorkerPool workerPool;
	private final WorkingClientPropertyEntity workingClientPropertyEntity;
	private Log logger = LogFactory.getFactory().getInstance(ConsumerBean.class);
	
	public ConsumerBean(WorkingClientPropertyEntity workingClientPropertyEntity, WorkerPool workerPool){
		this.workingClientPropertyEntity = workingClientPropertyEntity;
		this.workerPool = workerPool;
	}
	
	/**
	 * search the configuration check the configure for the service
	 * @param id
	 * @return
	 */
	private ClientPropertyEntity searchServiceClientEntity(String id){
		for(ClientPropertyEntity entity : this.workingClientPropertyEntity.getServiceClientList()){
			if(entity.getId().equals(id)){
				return entity;
			}
		}
		return null;
	}
	/**
	 *  search the route in the property route list
	 * @param entity
	 * @return
	 */
	private AbstractRoute searchRoute(String clientID){
		ClientPropertyEntity objServiceClientEntity = searchServiceClientEntity(clientID);
		if(objServiceClientEntity.getRouteid() == null || objServiceClientEntity.getRouteid() == "")
			return workingClientPropertyEntity.getDefaultRoute();
		for(AbstractRoute route : workingClientPropertyEntity.getRouteList()){
			if(route.getRouteid().equals(objServiceClientEntity.getRouteid())){
				return route;
			}
		}
		return workingClientPropertyEntity.getDefaultRoute();
	}
	
	/**
	 * send the request from the client to request a service
	 * @param clientID the id set in property
	 * @param args  the arguments for the service
	 * @return
	 * @throws Exception
	 */
	public RequestResultEntity prcessRequest(String clientID, List<String> args) {
		return prcessRequest(clientID, args, true);
	}
	
	/**
	 * send the request from the client to request a service
	 * this request will not reuse the channel
	 * this method is a synchronized method
	 * @param clientID the id set in property
	 * @param args  the arguments for the service
	 * @return
	 * @throws Exception
	 */
	public RequestResultEntity prcessRequestPerConnectSync(String clientID, List<String> args) {
		RequestResultEntity result = prcessRequest(clientID, args, false);
		result.getResponseEntity();
		this.closeChannelByRequestResult(result);
		return result;
	}
	
	/**
	 * send the request from the client to request a service
	 * this request will not reuse the channel
	 * use this method with closeChannelByRequestResult
	 * @param clientID the id set in property
	 * @param args  the arguments for the service
	 * @return
	 * @throws Exception
	 */
	public RequestResultEntity prcessRequestPerConnect(String clientID, List<String> args) {
		return prcessRequest(clientID, args, false);
	}
	
	/**
	 * send the request from the client to request a service
	 * @param clientID the id set in property
	 * @param args  the arguments for the service
	 * @param channelFromCached use the channel from the cached
	 * @return
	 * @throws Exception
	 */
	public RequestResultEntity prcessRequest(String clientID, List<String> args, boolean channelFromCached) {
		// this is unique id for a request
		long id = idGenerator.incrementAndGet();
		final RequestEntity objRequestEntity = createRequestEntity(clientID, args);
        RequestResultEntity result = new RequestResultEntity();
        result.setRequestID(objRequestEntity.getRequestID());
    	// Find the service information from the route, set the information into the result entity as well
        AbstractRoute route = searchRoute(clientID);
    	if(route == null){
			WorkingChannel.setExceptionToRuquestResult(result, new ServiceException(new Exception("Can not find the route"), "Can not find the route"));
			return result;
    	}
		ServiceInformationEntity serviceInformationEntity = null;
		try {
			serviceInformationEntity = route.chooseRoute(objRequestEntity, this);
			result.setServiceInformationEntity(serviceInformationEntity);
			if(serviceInformationEntity == null)
			{
				WorkingChannel.setExceptionToRuquestResult(result, new ServiceException(new Exception("Can not find the service"), "Can not find the service"));
				return result;
			}
		} 
		catch(Exception ex)
		{
			logger.error(StringUtils.ExceptionStackTraceToString(ex));
			//logger.log(Level.WARNING, ex.getMessage());
			//System.out.println("ComsumerBean ... exception happend " + ex.getMessage());
			//ex.printStackTrace();
        	WorkingChannel.setExceptionToRuquestResult(result, new ServiceException(ex, "ComsumerBean ... exception happend"));
        	route.clean(result);
        	return result;
        }
		return basicProcessRequest(objRequestEntity, result, serviceInformationEntity, channelFromCached);
	}
	
	/**
	 * send the request from the client to request a service
	 * this request will not reuse the channel
	 * this method is a synchronized method
	 * @param clientID the id set in property
	 * @param args  the arguments for the service
	 * @return
	 * @throws Exception
	 */
	public RequestResultEntity prcessRequestPerConnectSyncByServiceInformation(String clientID, List<String> args, ServiceInformationEntity serviceInformationEntity) {
		RequestResultEntity result = processRequestByServiceInformation(clientID, args, serviceInformationEntity, false);
		result.getResponseEntity();
		this.closeChannelByRequestResult(result);
		return result;
	}
	
	/**
	 * send the request from the client to request a service
	 * this request will not reuse the channel
	 * use this method with closeChannelByRequestResult
	 * @param clientID the id set in property
	 * @param args  the arguments for the service
	 * @return
	 * @throws Exception
	 */
	public RequestResultEntity prcessRequestPerConnectByServiceInformation(String clientID, List<String> args, 
			ServiceInformationEntity serviceInformationEntity) {
		return processRequestByServiceInformation(clientID, args, serviceInformationEntity, false);
	}
	
	/**
	 * request directly using the service information entity
	 * @param clientID
	 * @param args
	 * @param serviceInformationEntity
	 * @return
	 */
	public RequestResultEntity processRequestByServiceInformation(String clientID, List<String> args, 
			ServiceInformationEntity serviceInformationEntity)
	{
		return processRequestByServiceInformation(clientID, args, serviceInformationEntity, false);
	}
	
	/**
	 * request directly using the service information entity
	 * @param clientID
	 * @param args
	 * @param serviceInformationEntity
	 * @param channelFromCached
	 * @return
	 */
	public RequestResultEntity processRequestByServiceInformation(String clientID, List<String> args, 
			ServiceInformationEntity serviceInformationEntity, boolean channelFromCached)
	{
		RequestEntity objRequestEntity = createRequestEntity(clientID, args);
        RequestResultEntity result = new RequestResultEntity();
        result.setRequestID(objRequestEntity.getRequestID());
		return basicProcessRequest(objRequestEntity, result, serviceInformationEntity, channelFromCached);
	}
	
	/**
	 * basic process request
	 * @param objRequestEntity
	 * @param result
	 * @param serviceInformationEntity
	 * @param channelFromCached
	 * @return
	 */
	private RequestResultEntity basicProcessRequest(RequestEntity objRequestEntity, RequestResultEntity result, 
			ServiceInformationEntity serviceInformationEntity, boolean channelFromCached){
		WorkingChannel newWorkingChannel = null;
		try
		{
			newWorkingChannel = getWorkingChannnel(channelFromCached, serviceInformationEntity);
			result.setWorkingChannel(newWorkingChannel);
		}		
		catch(Exception ex){
			WorkingChannel.setExceptionToRuquestResult(result, new ServiceException(ex, ex.getMessage()));
			return result;
		}
		// put the request result into the request result list
		newWorkingChannel.offerRequestResult(result);
		ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(newWorkingChannel, objRequestEntity.getRequestID());
		String sendData = SerializeUtils.serializeRequest(objRequestEntity);
		objServiceOnMessageWriteEvent.setMessage(sendData);
		newWorkingChannel.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
		newWorkingChannel.getWorker().writeFromUser(newWorkingChannel);
		return result;
	}
	
	/**
	 * get a working channel from cache,
	 * if not existed, create a new one
	 * @param serviceName
	 * @param fromCached get it from the cache or not
	 * @return
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws Exception
	 */
	private WorkingChannel getWorkingChannnel(boolean fromCached, ServiceInformationEntity service) throws IOException, InterruptedException, ExecutionException, Exception  {
		if(service == null)
			return null;
		String cacheID = service.toString();
		WorkingChannel objWorkingChannel;
		// if not get it from the cache, create it directly
		if(!fromCached){
			objWorkingChannel  = createWorkingChannel(service);
			objWorkingChannel.setWorkingChannelCacheID(cacheID);
			return objWorkingChannel;
		}
		objWorkingChannel = workingChannelCacheList.get(cacheID);
		if(objWorkingChannel == null)
		{
			synchronized(workingChannelCacheList){
				// get the working channel again 
				// in case we set after we get from the cache
				objWorkingChannel = workingChannelCacheList.get(service.toString());
				if(objWorkingChannel == null)
				{
					objWorkingChannel = createWorkingChannel(service);
					objWorkingChannel.setWorkingChannelCacheID(cacheID);
					workingChannelCacheList.put(cacheID, objWorkingChannel);
				}
			}
		}
		return objWorkingChannel;
	}
	
	/**
	 * create a request entity
	 * @param clientID
	 * @param args
	 * @return
	 */
	private RequestEntity createRequestEntity(String clientID, List<String> args){
		long id = idGenerator.incrementAndGet();
		final RequestEntity objRequestEntity = new RequestEntity();
		ClientPropertyEntity objServiceClientEntity = searchServiceClientEntity(clientID);
		objRequestEntity.setMethodName(objServiceClientEntity.getServiceMethod());
		objRequestEntity.setGroup(objServiceClientEntity.getServiceGroup());
		objRequestEntity.setServiceName(objServiceClientEntity.getServiceName());
		objRequestEntity.setArgs(args);
		objRequestEntity.setRequestID("" + id);
		objRequestEntity.setRouteid(objServiceClientEntity.getRouteid());
		return objRequestEntity;
	}
	
	/**
	 *  connect to the service
	 * @param address
	 * @param port
	 * @return
	 * @throws IOException
	 */
	private WorkingChannel createWorkingChannel(ServiceInformationEntity service) throws IOException{
		// get a Socket channel
        SocketChannel channel = SocketChannel.open();  
        // connect
        channel.connect(new InetSocketAddress(service.getAddress(), service.getPort()));
        // finish the connect
        if(channel.isConnectionPending()){  
            channel.finishConnect();  
        } 
        // wait for the worker pool util it is ready
        this.workerPool.waitReady();
        WorkingChannel objWorkingChannel = this.workerPool.register(channel);
        return objWorkingChannel;
	}
	
	/**
	 * remove the channel from the cache 
	 * @param requestID
	 */
	public void removeCachedChannel(WorkingChannel objWorkingChannel){
		if(objWorkingChannel != null)
		{
			synchronized(workingChannelCacheList){
				this.workingChannelCacheList.remove(objWorkingChannel.getWoringChannelCacheID());
			}
		}
	}
	
	/**
	 * close the channel by request result
	 * the client could determine close the channel or not
	 * this method is used along with method prcessRequestPerConnect
	 * don't use it along with method prcessRequest
	 * @param objRequestResultEntity
	 */
	public void closeChannelByRequestResult(RequestResultEntity objRequestResultEntity){
		try {
			removeCachedChannel(objRequestResultEntity.getWorkingChannel());
			if(objRequestResultEntity.getWorkingChannel() != null && 
					objRequestResultEntity.getWorkingChannel().getWorker() != null)
			{
				objRequestResultEntity.getWorkingChannel().getWorker().closeWorkingChannel(objRequestResultEntity.getWorkingChannel());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
