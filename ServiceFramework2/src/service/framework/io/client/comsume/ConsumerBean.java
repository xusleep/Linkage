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
import service.framework.io.master.ClientBootStrap;
import service.framework.io.master.ServiceBootStrap;
import service.framework.io.server.DefaultWorkerPool;
import service.framework.io.server.WorkerPool;
import service.framework.io.server.WorkingChannel;
import service.framework.protocol.ShareingProtocolData;
import service.framework.provide.entity.RequestEntity;
import service.framework.provide.entity.ResponseEntity;
import service.framework.route.Route;
import service.framework.serialization.SerializeUtils;
import servicecenter.service.ServiceInformation;

public class ConsumerBean {
	public static ConcurrentHashMap<String, RequestResultEntity> resultList = new ConcurrentHashMap<String, RequestResultEntity>(16);
	private AtomicLong idGenerator = new AtomicLong(0);
	private String methodName;
	private String serviceName;
	private String version;
	private String group;
	private Route route;
	private WorkingChannel newWorkingChannel;
	private WorkerPool workerPool;
	
	public ConsumerBean(){

	}
	
	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}
	
	

	public WorkerPool getWorkerPool() {
		return workerPool;
	}

	public void setWorkerPool(WorkerPool objWorkerPool) {
		this.workerPool = objWorkerPool;
	}

	public synchronized void build() throws Exception{
		ServiceInformation service;
		service = this.route.chooseRoute(serviceName);
		this.newWorkingChannel = newWorkingChannel(service.getAddress(), service.getPort());
	}

	public RequestResultEntity prcessRequest(List<String> args) throws IOException, InterruptedException, ExecutionException{
		long id = idGenerator.incrementAndGet();
		final RequestEntity objRequestEntity = new RequestEntity();
		objRequestEntity.setMethodName(methodName);
		objRequestEntity.setGroup(group);
		objRequestEntity.setServiceName(serviceName);
		objRequestEntity.setArgs(args);
		objRequestEntity.setRequestID("" + id);
        RequestResultEntity result = new RequestResultEntity();
        result.setRequestID(objRequestEntity.getRequestID());
        resultList.put(objRequestEntity.getRequestID(), result);
    	ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(newWorkingChannel);
        String sendData = SerializeUtils.serializeRequest(objRequestEntity);
		objServiceOnMessageWriteEvent.setMessage(sendData);
        newWorkingChannel.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
        newWorkingChannel.getWorker().writeFromUser(newWorkingChannel);
    	return result;
	}
	
	public WorkingChannel newWorkingChannel(String address, int port) throws IOException{
		// ���һ��Socketͨ��  
        SocketChannel channel = SocketChannel.open();  
        // ����ͨ��Ϊ������  
        channel.configureBlocking(false);   
          
        // �ͻ������ӷ�����,��ʵ����ִ�в�û��ʵ�����ӣ���Ҫ��listen���������е�  
        //��channel.finishConnect();�����������  
        channel.connect(new InetSocketAddress(address, port));
        // ����������ӣ����������  
        if(channel.isConnectionPending()){  
            channel.finishConnect();  
        } 
        System.out.println("wait pool ready ...");
        this.getWorkerPool().waitReady();
        System.out.println("pool is ready!");
        WorkingChannel objWorkingChannel = this.getWorkerPool().register(channel);
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
