package service.framework.comsume;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import service.framework.common.SerializeUtils;
import service.framework.common.entity.RequestEntity;
import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ResponseEntity;
import service.framework.common.entity.ServiceInformationEntity;
import service.framework.event.ServiceOnMessageWriteEvent;
import service.framework.exception.ServiceException;
import service.framework.io.common.WorkerPool;
import service.framework.io.common.WorkingChannel;
import service.framework.properties.ClientPropertyEntity;
import service.framework.properties.WorkingPropertyEntity;
import service.framework.route.DefaultRoute;
import service.framework.route.Route;

/**
 * this class is an access point from the client
 * the client will use this class to call the service  
 * @author zhonxu
 *
 */
public class ConsumerBean {
	
	private final ConcurrentHashMap<String, RequestResultEntity> resultList = new ConcurrentHashMap<String, RequestResultEntity>(2048);
	private final HashMap<String, WorkingChannel> workingChannelCacheList = new HashMap<String, WorkingChannel>(16);
	private AtomicLong idGenerator = new AtomicLong(0);
	private final Route route;
	private final WorkerPool workerPool;
	private final WorkingPropertyEntity servicePropertyEntity;
	
	public ConsumerBean(WorkingPropertyEntity servicePropertyEntity, WorkerPool workerPool){
		this.route = new DefaultRoute(servicePropertyEntity, this);
		this.servicePropertyEntity = servicePropertyEntity;
		this.workerPool = workerPool;
	}
	
	/**
	 * search the configuration check the configure for the service
	 * @param id
	 * @return
	 */
	private ClientPropertyEntity searchServiceClientEntity(String id){
		for(ClientPropertyEntity entity : this.servicePropertyEntity.getServiceClientList()){
			if(entity.getId().equals(id)){
				return entity;
			}
		}
		return null;
	}
	
	/**
	 * send the request from the client to request a service
	 * @param clientID the id set in property
	 * @param args  the arguments for the service
	 * @return
	 * @throws Exception
	 */
	public synchronized RequestResultEntity prcessRequest(String clientID, List<String> args, boolean isClosingAfterRequest) {
		// this is unique id for a request
		long id = idGenerator.incrementAndGet();
		final RequestEntity objRequestEntity = new RequestEntity();
		ClientPropertyEntity objServiceClientEntity = searchServiceClientEntity(clientID);
		objRequestEntity.setMethodName(objServiceClientEntity.getServiceMethod());
		objRequestEntity.setGroup(objServiceClientEntity.getServiceGroup());
		objRequestEntity.setServiceName(objServiceClientEntity.getServiceName());
		objRequestEntity.setArgs(args);
		objRequestEntity.setRequestID("" + id);
        RequestResultEntity result = new RequestResultEntity();
        result.setRequestID(objRequestEntity.getRequestID());
        resultList.put(objRequestEntity.getRequestID(), result);
        WorkingChannel newWorkingChannel = null;
        try
        {
        	newWorkingChannel = getWorkingChannnel(objRequestEntity.getServiceName());
        }
        catch(Exception ex){
        	this.setExceptionRuquestResult(result.getRequestID(), new ServiceException(ex, ex.getMessage()));
        	return result;
        }
        if(newWorkingChannel == null)
        {
        	this.setExceptionRuquestResult(result.getRequestID(), new ServiceException(null, "can not connect to the service"));
        	return result;
        }
    	ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(newWorkingChannel, objRequestEntity.getRequestID());
        String sendData = SerializeUtils.serializeRequest(objRequestEntity);
		objServiceOnMessageWriteEvent.setMessage(sendData);
        newWorkingChannel.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
        newWorkingChannel.getWorker().writeFromUser(newWorkingChannel);
        result.setCloseingAfterRequest(isClosingAfterRequest);
    	return result;
	}
	
	
	/**
	 * get a working channel
	 * @param serviceName
	 * @return
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws Exception
	 */
	private WorkingChannel getWorkingChannnel(String serviceName) throws IOException, InterruptedException, ExecutionException, Exception  {
		ServiceInformationEntity service;
		service = this.route.chooseRoute(serviceName);
		if(service == null)
			return null;
		String cacheID = service.toString();
		WorkingChannel objWorkingChannel = workingChannelCacheList.get(cacheID);
		if(objWorkingChannel == null)
		{
			synchronized(workingChannelCacheList){
				// get the working channel again 
				// in case we set after we get from the cache
				objWorkingChannel = workingChannelCacheList.get(service.toString());
				if(objWorkingChannel == null)
				{
					objWorkingChannel = createWorkingChannel(service.getAddress(), service.getPort());
					objWorkingChannel.setCacheID(cacheID);
					workingChannelCacheList.put(cacheID, objWorkingChannel);
				}
			}
		}
		return objWorkingChannel;
	}
	
	/**
	 *  connect to the service
	 * @param address
	 * @param port
	 * @return
	 * @throws IOException
	 */
	private WorkingChannel createWorkingChannel(String address, int port) throws IOException{
		// get a Socket channel
        SocketChannel channel = SocketChannel.open();  
        // connect
        channel.connect(new InetSocketAddress(address, port));
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
	 * get the proterties
	 * @return
	 */
	public WorkingPropertyEntity getServicePropertyEntity() {
		return servicePropertyEntity;
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
	 * remove the colsed channel from the cache when there is a error comes out
	 * @param requestID
	 */
	public void removeClosedChannel(WorkingChannel objWorkingChannel){
		synchronized(workingChannelCacheList){
			this.workingChannelCacheList.remove(objWorkingChannel.getCacheID());
		}
	}
	
	public void closeChannel(WorkingChannel objWorkingChannel){
		System.out.println("close the channel");
		removeClosedChannel(objWorkingChannel);
		try {
			objWorkingChannel.getChannel().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
