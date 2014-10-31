package service.framework.io.client.comsume;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import service.framework.common.entity.RequestEntity;
import service.framework.io.event.ServiceOnMessageWriteEvent;
import service.framework.io.server.WorkerPool;
import service.framework.io.server.WorkingChannel;
import service.framework.route.DefaultRoute;
import service.framework.route.Route;
import service.framework.serialization.SerializeUtils;
import service.properties.ServiceClientEntity;
import service.properties.ServicePropertyEntity;
import servicecenter.service.ServiceInformation;

public class ConsumerBean {
	
	private final ConcurrentHashMap<String, RequestResultEntity> resultList = new ConcurrentHashMap<String, RequestResultEntity>(2048);
	private final ConcurrentHashMap<String, WorkingChannel> workingChannelCacheList = new ConcurrentHashMap<String, WorkingChannel>(16);
	private AtomicLong idGenerator = new AtomicLong(0);
	private final Route route;
	private final WorkerPool workerPool;
	private final ServicePropertyEntity servicePropertyEntity;
	
	public ConsumerBean(ServicePropertyEntity servicePropertyEntity, WorkerPool workerPool){
		this.route = new DefaultRoute(servicePropertyEntity, this);
		this.servicePropertyEntity = servicePropertyEntity;
		this.workerPool = workerPool;
	}

	public WorkerPool getWorkerPool() {
		return workerPool;
	}

	
	
	private ServiceClientEntity searchServiceClientEntity(String id){
		for(ServiceClientEntity entity : this.servicePropertyEntity.getServiceClientList()){
			if(entity.getId().equals(id)){
				return entity;
			}
		}
		return null;
	}

	public synchronized RequestResultEntity prcessRequest(String clientID, List<String> args) throws Exception{
		long id = idGenerator.incrementAndGet();
		System.out.println("request clientid = " + clientID);
		final RequestEntity objRequestEntity = new RequestEntity();
		ServiceClientEntity objServiceClientEntity = searchServiceClientEntity(clientID);
		objRequestEntity.setMethodName(objServiceClientEntity.getServiceMethod());
		objRequestEntity.setGroup(objServiceClientEntity.getServiceGroup());
		objRequestEntity.setServiceName(objServiceClientEntity.getServiceName());
		objRequestEntity.setArgs(args);
		objRequestEntity.setRequestID("" + id);
        RequestResultEntity result = new RequestResultEntity();
        result.setRequestID(objRequestEntity.getRequestID());
        resultList.put(objRequestEntity.getRequestID(), result);
        WorkingChannel newWorkingChannel = createWorkingChannnel(objRequestEntity.getServiceName());
    	ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(newWorkingChannel);
        String sendData = SerializeUtils.serializeRequest(objRequestEntity);
		objServiceOnMessageWriteEvent.setMessage(sendData);
        newWorkingChannel.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
        newWorkingChannel.getWorker().writeFromUser(newWorkingChannel);
    	return result;
	}
	
	private WorkingChannel createWorkingChannnel(String serviceName) throws Exception{
		ServiceInformation service;
		service = this.route.chooseRoute(serviceName);
		WorkingChannel objWorkingChannel = workingChannelCacheList.get(service.toString());
		if(objWorkingChannel == null)
		{
			synchronized(this){
				// get the working channel again 
				// in case we set after we get from the cache
				objWorkingChannel = workingChannelCacheList.get(service.toString());
				if(objWorkingChannel == null)
				{
					objWorkingChannel = createWorkingChannel(service.getAddress(), service.getPort());
					workingChannelCacheList.put(service.toString(), objWorkingChannel);
				}
			}
		}
		return objWorkingChannel;
	}
	
	public WorkingChannel createWorkingChannel(String address, int port) throws IOException{
		// 获得一个Socket通道  
        SocketChannel channel = SocketChannel.open();  
        // 设置通道为非阻塞  
        channel.configureBlocking(false);   
          
        // 客户端连接服务器,其实方法执行并没有实现连接，需要在listen（）方法中调  
        //用channel.finishConnect();才能完成连接  
        channel.connect(new InetSocketAddress(address, port));
        // 如果正在连接，则完成连接  
        if(channel.isConnectionPending()){  
            channel.finishConnect();  
        } 
        this.getWorkerPool().waitReady();
        WorkingChannel objWorkingChannel = this.getWorkerPool().register(channel);
        return objWorkingChannel;
	}

	public ConcurrentHashMap<String, RequestResultEntity> getResultList() {
		return resultList;
	}

	public ServicePropertyEntity getServicePropertyEntity() {
		return servicePropertyEntity;
	}
	
	
}
