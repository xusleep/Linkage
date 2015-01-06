package service.middleware.linkage.center.bootstrap;

import java.io.IOException;

import service.middleware.linkage.center.comsume.NIORouteServiceAccess;
import service.middleware.linkage.framework.common.entity.ServiceInformationEntity;
import service.middleware.linkage.framework.distribution.EventDistributionMaster;
import service.middleware.linkage.framework.handlers.ClientReadWriteHandler;
import service.middleware.linkage.framework.io.client.Client;
import service.middleware.linkage.framework.io.client.DefaultClient;
import service.middleware.linkage.framework.io.common.NIOWorkerPool;
import service.middleware.linkage.framework.io.common.WorkerPool;
import service.middleware.linkage.framework.setting.reader.ClientSettingPropertyReader;
import service.middleware.linkage.framework.setting.reader.ClientSettingReader;

/**
 * client side boot strap
 * it will init a worker pool and a distribution master
 * @author zhonxu
 *
 */
public class NIOCenterClientBootStrap implements Runnable {
	private final Client client;
	private final EventDistributionMaster eventDistributionHandler;
	private final WorkerPool workPool;
	private final NIORouteServiceAccess serviceAccess;
	
	/**
	 * 
	 * @param propertyPath the property configured for the client
	 * @param clientTaskThreadPootSize the client 
	 */
	public NIOCenterClientBootStrap(String propertyPath, int clientTaskThreadPootSize, ServiceInformationEntity centerServiceInformationEntity){
		// read the configuration from the properties
		ClientSettingReader objServicePropertyEntity = null;
		try {
			objServicePropertyEntity = new ClientSettingPropertyReader(propertyPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// new a task handler, this handler will handle all of the task from the pool queue
		// into the executor pool(thread pool) which will execute the task.
		eventDistributionHandler = new EventDistributionMaster(clientTaskThreadPootSize);
		// this is a client worker pool, this pool will handle all of the io operation 
		// with the server
		this.workPool = new NIOWorkerPool(eventDistributionHandler, 1);
		// this is a client, in this client it will be a gather place where we will start the worker pool & task handler 
		this.client = new DefaultClient(eventDistributionHandler, this.workPool);
		this.serviceAccess = new NIORouteServiceAccess(objServicePropertyEntity, this.workPool, centerServiceInformationEntity);
		eventDistributionHandler.registerHandler(new ClientReadWriteHandler(this.getServiceAccess()));
	}
	
	/**
	 * the user can use this cosumer bean to request service
	 * from the client
	 * @return
	 */
	public NIORouteServiceAccess getServiceAccess() {
		return serviceAccess;
	}

	public WorkerPool getWorkPool() {
		return workPool;
	}

	@Override
	public void run() {
		new Thread(client).start();
	}

	public EventDistributionMaster getEventDistributionHandler() {
		return eventDistributionHandler;
	}
	
	/**
	 * shutdown 
	 */
	public void shutdown()
	{
		client.shutdown();
	}
	/**
	 * shutdown imediate
	 */
	public void shutdownImediate()
	{
		client.shutdownImediate();
	}
}
