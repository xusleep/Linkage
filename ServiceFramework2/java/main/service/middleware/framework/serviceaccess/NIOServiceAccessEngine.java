package service.middleware.framework.serviceaccess;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import service.middleware.framework.common.SerializeUtils;
import service.middleware.framework.common.entity.RequestEntity;
import service.middleware.framework.common.entity.RequestResultEntity;
import service.middleware.framework.common.entity.ResponseEntity;
import service.middleware.framework.common.entity.ServiceInformationEntity;
import service.middleware.framework.event.ServiceOnMessageWriteEvent;
import service.middleware.framework.exception.ServiceException;
import service.middleware.framework.io.common.NIOWorkingChannel;
import service.middleware.framework.io.common.WorkerPool;
import service.middleware.framework.io.common.WorkingChannel;
import service.middleware.framework.setting.ClientSettingEntity;
import service.middleware.framework.setting.reader.ClientSettingReader;

/**
 * this an engine class of consume
 * @author zhonxu
 *
 */
public class NIOServiceAccessEngine{
	/**
	 * used to cached the {@link WorkingChannel} object
	 */
	private final HashMap<String, WorkingChannel> workingChannelCacheList = new HashMap<String, WorkingChannel>(16);
	private AtomicLong idGenerator = new AtomicLong(0);
	private final WorkerPool workerPool;
	private final ClientSettingReader workingClientPropertyEntity;
	
	public NIOServiceAccessEngine(ClientSettingReader workingClientPropertyEntity, WorkerPool workerPool){
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
		NIOWorkingChannel newWorkingChannel = null;
		try
		{
			newWorkingChannel = (NIOWorkingChannel) getWorkingChannnel(channelFromCached, serviceInformationEntity);
			result.setWorkingChannel(newWorkingChannel);
		}		
		catch(Exception ex){
			NIOWorkingChannel.setExceptionToRuquestResult(result, new ServiceException(ex, ex.getMessage()));
			return result;
		}
		// put the request result into the request result list
		newWorkingChannel.offerRequestResult(result);
		ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(newWorkingChannel, objRequestEntity.getRequestID());
		String sendData = SerializeUtils.serializeRequest(objRequestEntity);
		objServiceOnMessageWriteEvent.setMessage(sendData);
		newWorkingChannel.offerWriterQueue(objServiceOnMessageWriteEvent);
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
		NIOWorkingChannel objWorkingChannel;
		// if not get it from the cache, create it directly
		if(!fromCached){
			objWorkingChannel  = createWorkingChannel(service);
			objWorkingChannel.setWorkingChannelCacheID(cacheID);
			return objWorkingChannel;
		}
		objWorkingChannel = (NIOWorkingChannel) workingChannelCacheList.get(cacheID);
		if(objWorkingChannel == null)
		{
			synchronized(workingChannelCacheList){
				// get the working channel again 
				// in case we set after we get from the cache
				objWorkingChannel = (NIOWorkingChannel) workingChannelCacheList.get(service.toString());
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
	private NIOWorkingChannel createWorkingChannel(ServiceInformationEntity service) throws IOException{
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
        NIOWorkingChannel objWorkingChannel = (NIOWorkingChannel) this.workerPool.register(channel);
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
				this.workingChannelCacheList.remove(((NIOWorkingChannel)objWorkingChannel).getWoringChannelCacheID());
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
