package service.framework.io.client.comsume;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import service.framework.io.client.ClientTask;
import service.framework.provide.entity.RequestEntity;
import service.framework.provide.entity.ResponseEntity;
import service.framework.route.Route;
import servicecenter.service.ServiceInformation;

public class ConsumerBean {
	private ConcurrentHashMap resultList = new ConcurrentHashMap(16);
	private AtomicLong idGenerator = new AtomicLong(0);
	private String methodName;
	private String serviceName;
	private String version;
	private String group;
	private Route route;
	
	public ConsumerBean(){
		
	}
	
	public ConsumerBean(Route route){
		this.route = route;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public long prcessRequest(List<String> args) throws IOException, InterruptedException, ExecutionException{
		long id = idGenerator.incrementAndGet();
		RequestEntity objRequestEntity = new RequestEntity();
		objRequestEntity.setMethodName(methodName);
		objRequestEntity.setGroup(group);
		objRequestEntity.setServiceName(serviceName);
		objRequestEntity.setArgs(args);
		ServiceInformation service = route.chooseRoute(serviceName);
    	ClientTask task = new ClientTask(service.getAddress(), service.getPort(), objRequestEntity);
    	Future result = ClientManagement.objExecutorService.submit(task);
    	ResponseEntity objResponseEntity = (ResponseEntity)result.get();
    	resultList.put(id, result);
    	return id;
	}
	
	public String getResult(long id) throws InterruptedException, ExecutionException{
		Future result = (Future)resultList.get(id);
		ResponseEntity objResponseEntity = (ResponseEntity)result.get();
		return objResponseEntity.getResult();
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
