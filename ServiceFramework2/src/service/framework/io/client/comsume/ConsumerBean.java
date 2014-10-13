package service.framework.io.client.comsume;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import service.framework.io.client.ClientTask;
import service.framework.io.event.ServiceOnMessageWriteEvent;
import service.framework.io.server.WorkerPool;
import service.framework.io.server.WorkingChannel;
import service.framework.protocol.ShareingProtocolData;
import service.framework.provide.entity.RequestEntity;
import service.framework.provide.entity.ResponseEntity;
import service.framework.route.Route;
import service.framework.serialization.SerializeUtils;
import servicecenter.service.ServiceInformation;

public class ConsumerBean {
	private ConcurrentHashMap resultList = new ConcurrentHashMap(16);
	private AtomicLong idGenerator = new AtomicLong(0);
	private String methodName;
	private String serviceName;
	private String version;
	private String group;
	private Route route;
	private WorkingChannel newWorkingChannel;
	
	public ConsumerBean(){

	}
	
	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public void build(){
		try {
			ServiceInformation service;
			service = this.route.chooseRoute(serviceName);
			this.newWorkingChannel = newWorkingChannel(service.getAddress(), service.getPort());
		} catch (IOException | InterruptedException | ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public long prcessRequest(List<String> args) throws IOException, InterruptedException, ExecutionException{
		long id = idGenerator.incrementAndGet();
		final RequestEntity objRequestEntity = new RequestEntity();
		objRequestEntity.setMethodName(methodName);
		objRequestEntity.setGroup(group);
		objRequestEntity.setServiceName(serviceName);
		objRequestEntity.setArgs(args);

    	new Thread(new Runnable(){

			@Override
			public void run() {
					// TODO Auto-generated method stub
					ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(newWorkingChannel);
			        String sendData = SerializeUtils.serializeRequest(objRequestEntity);
					objServiceOnMessageWriteEvent.setMessage(sendData);
					
			        newWorkingChannel.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
			        newWorkingChannel.getWorker().writeFromUser(newWorkingChannel);
			}
    		
    	}).start();
    	//ResponseEntity objResponseEntity = (ResponseEntity)result.get();
    	//resultList.put(id, result);
    	return id;
	}
	
	public String getResult(long id) throws InterruptedException, ExecutionException{
		
		return "";
	}
	
	public WorkingChannel newWorkingChannel(String address, int port) throws IOException{
		WorkerPool.getInstance().start();
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
        
        WorkingChannel objWorkingChannel = WorkerPool.getInstance().register(channel);
        return objWorkingChannel;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
}
