package service.middleware.linkage.framework.serviceaccess;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import service.middleware.linkage.framework.connection.pool.NIOConnectionPoolManager;
import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.io.WorkerPool;
import service.middleware.linkage.framework.io.WorkingChannelContext;
import service.middleware.linkage.framework.io.nio.NIOWorkingChannelContext;
import service.middleware.linkage.framework.io.nio.strategy.WorkingChannelMode;
import service.middleware.linkage.framework.io.nio.strategy.mixed.NIOMixedStrategy;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnMessageDataWriteEvent;
import service.middleware.linkage.framework.serialization.SerializationUtils;
import service.middleware.linkage.framework.serviceaccess.entity.RequestEntity;
import service.middleware.linkage.framework.serviceaccess.entity.RequestResultEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ResponseEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;
import service.middleware.linkage.framework.setting.ClientSettingEntity;
import service.middleware.linkage.framework.setting.reader.ClientSettingReader;
import service.middleware.linkage.framework.utils.EncodingUtils;

/**
 * this an engine class of consume
 * @author zhonxu
 *
 */
public class ServiceAccessEngineImpl implements ServiceAccessEngine{
	/**
	 * used to cached the {@link WorkingChannelContext} object
	 */
	private final HashMap<String, WorkingChannelContext> workingChannelCacheList = new HashMap<String, WorkingChannelContext>(16);
	private AtomicLong idGenerator = new AtomicLong(0);
	private final WorkerPool workerPool;
	private final ClientSettingReader workingClientPropertyEntity;
	 // use the concurrent hash map to store the request result list {@link RequestResultEntity}
	private final ConcurrentHashMap<String, RequestResultEntity> resultList = new ConcurrentHashMap<String, RequestResultEntity>(2048);
	
	public ServiceAccessEngineImpl(ClientSettingReader workingClientPropertyEntity, WorkerPool workerPool){
		this.workingClientPropertyEntity = workingClientPropertyEntity;
		this.workerPool = workerPool;
	}
	
	/**
	 * put the request result in the result list
	 * @param requestResultEntity
	 */
	public void offerRequestResult(RequestResultEntity requestResultEntity){
		resultList.put(requestResultEntity.getRequestID(), requestResultEntity);
	}
	
	/**
	 * clear the result
	 */
	public void clearAllResult(ServiceException exception){
		Enumeration<String> keyEnumeration = resultList.keys();
		while(keyEnumeration.hasMoreElements())
		{
			String requestID = keyEnumeration.nextElement();
			RequestResultEntity requestResultEntity = resultList.get(requestID);
			setExceptionToRuquestResult(requestResultEntity, exception);
		}
		resultList.clear();
	}
	
	/**
	 * set the request result
	 * @param requestID
	 * @param strResult
	 */
	public void setRuquestResult(String requestID, String strResult){
		RequestResultEntity result = this.resultList.remove(requestID);
		if(result != null)
		{
		    ResponseEntity objResponseEntity = new ResponseEntity();
		    objResponseEntity.setRequestID(requestID);
		    objResponseEntity.setResult(strResult);
		    result.setException(false);
			result.setResponseEntity(objResponseEntity);
		}
	}
	
	/**
	 * set the request result
	 * @param requestID
	 * @param strResult
	 */
	public void setExceptionRuquestResult(String requestID, ServiceException serviceException){
		RequestResultEntity result = this.resultList.remove(requestID);
		if(result != null)
		{
		    ResponseEntity objResponseEntity = new ResponseEntity();
		    objResponseEntity.setRequestID(requestID);
		    objResponseEntity.setResult(serviceException.getMessage());
		    result.setException(true);
		    result.setException(serviceException);
			result.setResponseEntity(objResponseEntity);
		}
	}
	
	/**
	 * when the response comes, use this method to set it. 
	 * @param objResponseEntity
	 */
	public RequestResultEntity setRequestResult(ResponseEntity objResponseEntity){
		RequestResultEntity result = this.resultList.remove(objResponseEntity.getRequestID());
		if(result != null)
		{
			result.setException(false);
			result.setResponseEntity(objResponseEntity);
		}
		return result;
	}
	
	/**
	 * search the configuration check the configure for the service
	 * @param id
	 * @return
	 */
	protected ClientSettingEntity searchServiceClientEntity(String id){
		for(ClientSettingEntity entity : this.workingClientPropertyEntity.getServiceClientList()){
			if(entity.getId().equals(id)){
				return entity;
			}
		}
		return null;
	}
	
	/**
	 * basic process request
	 * @param objRequestEntity
	 * @param result
	 * @param serviceInformationEntity
	 * @param channelFromCached
	 * @return
	 */
	public RequestResultEntity basicProcessRequest(RequestEntity objRequestEntity, RequestResultEntity result, 
			ServiceInformationEntity serviceInformationEntity, boolean channelFromCached){
		NIOWorkingChannelContext newWorkingChannel = null;
		NIOMixedStrategy strategy = null;
		try
		{
			newWorkingChannel = (NIOWorkingChannelContext) getWorkingChannnel(channelFromCached, serviceInformationEntity);
			result.setWorkingChannel(newWorkingChannel);
			strategy = (NIOMixedStrategy)newWorkingChannel.getWorkingChannelStrategy();
		}		
		catch(Exception ex){
			setExceptionToRuquestResult(result, new ServiceException(ex, ex.getMessage()));
			return result;
		}
		// put the request result into the request result list
		this.offerRequestResult(result);
		
		String sendData = SerializationUtils.serializeRequest(objRequestEntity);
		ServiceOnMessageDataWriteEvent objServiceOnMessageWriteEvent;
		try {
			objServiceOnMessageWriteEvent = new ServiceOnMessageDataWriteEvent(newWorkingChannel, sendData.getBytes(EncodingUtils.FRAMEWORK_IO_ENCODING));
			strategy.offerMessageWriteQueue(objServiceOnMessageWriteEvent);
			strategy.writeChannel();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	//public RequestResultEntity uploadFile(String )
	
	/**
	 * get a working channel from cache,
	 * if not existed, create a new one
	 * @param fromCached get it from the cache or not
	 * @param serviceName
	 * @return
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws Exception
	 */
	private WorkingChannelContext getWorkingChannnel(boolean fromCached, ServiceInformationEntity service) throws IOException, InterruptedException, ExecutionException, Exception  {
		if(service == null)
			return null;
		String cacheID = service.toString();
		NIOWorkingChannelContext objWorkingChannel;
		// if not get it from the cache, create it directly
		if(!fromCached){
			objWorkingChannel  = createWorkingChannel(service, WorkingChannelMode.MIXED);
			objWorkingChannel.setWorkingChannelCacheID(cacheID);
			return objWorkingChannel;
		}
		objWorkingChannel = (NIOWorkingChannelContext) workingChannelCacheList.get(cacheID);
		if(objWorkingChannel == null)
		{
			synchronized(workingChannelCacheList){
				// get the working channel again 
				// in case we set after we get from the cache
				objWorkingChannel = (NIOWorkingChannelContext) workingChannelCacheList.get(service.toString());
				if(objWorkingChannel == null)
				{
					objWorkingChannel = createWorkingChannel(service, WorkingChannelMode.MIXED);
					objWorkingChannel.setWorkingChannelCacheID(cacheID);
					workingChannelCacheList.put(cacheID, objWorkingChannel);
				}
			}
		}
		return objWorkingChannel;
	}
	
	/**
	 * set the request result
	 * @param requestID
	 * @param strResult
	 */
	public static void setExceptionToRuquestResult(RequestResultEntity result, ServiceException serviceException){
		if(result != null)
		{
		    ResponseEntity objResponseEntity = new ResponseEntity();
		    objResponseEntity.setRequestID(result.getRequestID());
		    objResponseEntity.setResult(serviceException.getMessage());
		    result.setException(true);
		    result.setException(serviceException);
			result.setResponseEntity(objResponseEntity);
		}
	}
	
	/**
	 * create a request entity
	 * @param clientID
	 * @param args
	 * @return
	 */
	public RequestEntity createRequestEntity(String clientID, List<String> args){
		long id = idGenerator.incrementAndGet();
		final RequestEntity objRequestEntity = new RequestEntity();
		ClientSettingEntity objServiceClientEntity = searchServiceClientEntity(clientID);
		objRequestEntity.setMethodName(objServiceClientEntity.getServiceMethod());
		objRequestEntity.setGroup(objServiceClientEntity.getServiceGroup());
		objRequestEntity.setServiceName(objServiceClientEntity.getServiceName());
		objRequestEntity.setArgs(args);
		objRequestEntity.setRequestID("" + id);
		return objRequestEntity;
	}
	
	/**
	 *  connect to the service
	 * @param address
	 * @param port
	 * @return
	 * @throws IOException
	 * @throws ServiceException 
	 */
	private NIOWorkingChannelContext createWorkingChannel(ServiceInformationEntity service, WorkingChannelMode workingChannelMode) throws IOException, ServiceException{
		SocketChannel channel = NIOConnectionPoolManager.getIOConnection(service.getAddress() + ":" + service.getPort());
        // wait for the worker pool util it is ready
        this.workerPool.waitReady();
        NIOWorkingChannelContext objWorkingChannel = (NIOWorkingChannelContext) this.workerPool.register(channel, workingChannelMode);
        return objWorkingChannel;
	}
	
	/**
	 * remove the channel from the cache 
	 * @param requestID
	 */
	public void removeCachedChannel(WorkingChannelContext objWorkingChannel){
		if(objWorkingChannel != null)
		{
			synchronized(workingChannelCacheList){
				this.workingChannelCacheList.remove(((NIOWorkingChannelContext)objWorkingChannel).getWoringChannelCacheID());
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
		removeCachedChannel(objRequestResultEntity.getWorkingChannel());
		if(objRequestResultEntity.getWorkingChannel() != null)
		{
			objRequestResultEntity.getWorkingChannel().closeWorkingChannel();
		}
	}
}
