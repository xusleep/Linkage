package service.middleware.linkage.framework.serviceaccess;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import service.middleware.linkage.framework.common.entity.RequestEntity;
import service.middleware.linkage.framework.common.entity.RequestResultEntity;
import service.middleware.linkage.framework.common.entity.ResponseEntity;
import service.middleware.linkage.framework.common.entity.ServiceInformationEntity;
import service.middleware.linkage.framework.event.ServiceOnMessageWriteEvent;
import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.io.common.NIOFileWorkingChannelStrategy;
import service.middleware.linkage.framework.io.common.NIOMessageWorkingChannelStrategy;
import service.middleware.linkage.framework.io.common.NIOWorkingChannelContext;
import service.middleware.linkage.framework.io.common.WorkerPool;
import service.middleware.linkage.framework.io.common.WorkingChannelContext;
import service.middleware.linkage.framework.io.common.WorkingChannelMode;
import service.middleware.linkage.framework.io.common.WorkingChannelModeUtils;
import service.middleware.linkage.framework.serialization.SerializationUtils;
import service.middleware.linkage.framework.setting.ClientSettingEntity;
import service.middleware.linkage.framework.setting.reader.ClientSettingReader;

/**
 * this an engine class of consume
 * @author zhonxu
 *
 */
public class NIOMessageModeServiceAccessEngine{
	/**
	 * used to cached the {@link WorkingChannelContext} object
	 */
	private final HashMap<String, WorkingChannelContext> workingChannelCacheList = new HashMap<String, WorkingChannelContext>(16);
	private AtomicLong idGenerator = new AtomicLong(0);
	private final WorkerPool workerPool;
	private final ClientSettingReader workingClientPropertyEntity;
	
	public NIOMessageModeServiceAccessEngine(ClientSettingReader workingClientPropertyEntity, WorkerPool workerPool){
		this.workingClientPropertyEntity = workingClientPropertyEntity;
		this.workerPool = workerPool;
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
		NIOMessageWorkingChannelStrategy strategy = null;
		try
		{
			newWorkingChannel = (NIOWorkingChannelContext) getWorkingChannnel(channelFromCached, serviceInformationEntity);
			result.setWorkingChannel(newWorkingChannel);
			strategy = (NIOMessageWorkingChannelStrategy)newWorkingChannel.getWorkingChannelStrategy();
		}		
		catch(Exception ex){
			NIOMessageWorkingChannelStrategy.setExceptionToRuquestResult(result, new ServiceException(ex, ex.getMessage()));
			return result;
		}
		// put the request result into the request result list
		strategy.offerRequestResult(result);
		ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(newWorkingChannel, objRequestEntity.getRequestID());
		String sendData = SerializationUtils.serializeRequest(objRequestEntity);
		objServiceOnMessageWriteEvent.setMessage(sendData);
		strategy.offerWriterQueue(objServiceOnMessageWriteEvent);
		strategy.writeChannel();
		return result;
	}
	
	public RequestResultEntity writeFile(File file, ServiceInformationEntity serviceInformationEntity, boolean channelFromCached){
		NIOWorkingChannelContext newWorkingChannel = null;
		RequestResultEntity result = new RequestResultEntity();
		NIOFileWorkingChannelStrategy strategy = null;
		try
		{
			newWorkingChannel = (NIOWorkingChannelContext) getWorkingChannnel(channelFromCached, serviceInformationEntity);
			if(newWorkingChannel.getWorkingChannelMode() == WorkingChannelMode.MESSAGEMODE){
				NIOMessageWorkingChannelStrategy msgStrategy = (NIOMessageWorkingChannelStrategy)newWorkingChannel.getWorkingChannelStrategy();
				ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(newWorkingChannel, null);
				objServiceOnMessageWriteEvent.setMessage(WorkingChannelModeUtils.getModeSwitchString(WorkingChannelMode.FILEMODE));
				msgStrategy.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
				msgStrategy.writeChannel();
			}
			//result.setWorkingChannel(newWorkingChannel);
			//strategy = (NIOFileWorkingChannelStrategy) newWorkingChannel.getWorkingChannelStrategy();
			//strategy.writeFileQueue.offer(file);
			//strategy.writeChannel();
		}
		catch(Exception ex){
			NIOMessageWorkingChannelStrategy.setExceptionToRuquestResult(result, new ServiceException(ex, ex.getMessage()));
			return result;
		}
		return null;
	}
	
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
			objWorkingChannel  = createWorkingChannel(service, WorkingChannelMode.MESSAGEMODE);
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
					objWorkingChannel = createWorkingChannel(service, WorkingChannelMode.MESSAGEMODE);
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
	 */
	private NIOWorkingChannelContext createWorkingChannel(ServiceInformationEntity service, WorkingChannelMode workingChannelMode) throws IOException{
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
