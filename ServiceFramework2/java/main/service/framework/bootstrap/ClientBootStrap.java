package service.framework.bootstrap;

import service.framework.comsume.ConsumerBean;
import service.framework.distribution.EventDistributionMaster;
import service.framework.handlers.ClientReadWriteHandler;
import service.framework.io.client.Client;
import service.framework.io.client.DefaultClient;
import service.framework.io.common.DefaultWorkerPool;
import service.framework.io.common.WorkerPool;
import service.framework.properties.WorkingClientPropertyEntity;
import service.framework.properties.WorkingServicePropertyEntity;

/**
 * client side boot strap
 * it will init a worker pool and a distribution master
 * @author zhonxu
 *
 */
public class ClientBootStrap implements Runnable {
	private final Client client;
	private final EventDistributionMaster eventDistributionHandler;
	private final WorkerPool workPool;
	private final ConsumerBean consumerBean;
	
	/**
	 * 
	 * @param propertyPath the property configured for the client
	 * @param clientTaskThreadPootSize the client 
	 */
	public ClientBootStrap(String propertyPath, int clientTaskThreadPootSize){
		// read the configuration from the properties
		WorkingClientPropertyEntity objServicePropertyEntity = new WorkingClientPropertyEntity(propertyPath);
		// new a task handler, this handler will handle all of the task from the pool queue
		// into the executor pool(thread pool) which will execute the task.
		eventDistributionHandler = new EventDistributionMaster(clientTaskThreadPootSize);
		// this is a client worker pool, this pool will handle all of the io operation 
		// with the server
		this.workPool = new DefaultWorkerPool(eventDistributionHandler);
		// this is a client, in this client it will be a gather place where we will start the worker pool & task handler 
		this.client = new DefaultClient(eventDistributionHandler, this.workPool);
		this.consumerBean = new ConsumerBean(objServicePropertyEntity, this.workPool);
		eventDistributionHandler.registerHandler(new ClientReadWriteHandler(this.getConsumerBean()));
	}
	
	/**
	 * the user can use this cosumer bean to request service
	 * from the client
	 * @return
	 */
	public ConsumerBean getConsumerBean() {
		return consumerBean;
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
}
