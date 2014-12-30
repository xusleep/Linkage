package service.framework.bootstrap;

import service.framework.distribution.EventDistributionMaster;
import service.framework.handlers.ServiceReadWriteHandler;
import service.framework.io.common.NIOWorkerPool;
import service.framework.io.common.WorkerPool;
import service.framework.io.server.DefaultServer;
import service.framework.io.server.Server;
import service.framework.properties.WorkingServicePropertyEntity;
import service.framework.provider.DefaultProvider;
import service.framework.provider.Provider;

/**
 * this is boot strap used from the server side
 * @author zhonxu
 *
 */
public class NIOServerBootStrap implements Runnable {
	private final Server server;
	private final Provider providerBean;
	private final EventDistributionMaster eventDistributionHandler;
	private final WorkingServicePropertyEntity servicePropertyEntity;
	
	public NIOServerBootStrap(String propertyPath, int serviceTaskThreadPootSize) throws Exception{
		// read the configuration from the properties
		this.servicePropertyEntity = new WorkingServicePropertyEntity(propertyPath);
		// new a task handler, this handler will handle all of the task from the pool queue
		// into the executor pool(thread pool) which will execute the task.
		this.eventDistributionHandler = new EventDistributionMaster(serviceTaskThreadPootSize);
		// this worker pool will handle the read write io operation for all of the connection
		WorkerPool workerPool = new NIOWorkerPool(eventDistributionHandler);
		// this is a provider which provides the service point access from the io layer
		// in this provider, all of the service information will load into the bean
		// when there is a request, the provider will find the service, init it & execute the service
		this.providerBean = new DefaultProvider(servicePropertyEntity);
		// this is a handler for the service, which will read the requestion information & call the provider 
		// to handle further
		eventDistributionHandler.registerHandler(new ServiceReadWriteHandler(providerBean));
		
		// this is the server, it will accept all of the connection & register the channel into the worker pool
		this.server = new DefaultServer(servicePropertyEntity.getServiceAddress(), servicePropertyEntity.getServicePort(),
				eventDistributionHandler, workerPool);
	}

	public Provider getProviderBean() {
		return providerBean;
	}

	public EventDistributionMaster getEventDistributionHandler() {
		return eventDistributionHandler;
	}

	public WorkingServicePropertyEntity getServicePropertyEntity() {
		return servicePropertyEntity;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		new Thread(server).start();
	}
	
	/**
	 * shutdown 
	 */
	public void shutdown()
	{
		server.shutdown();
	}
	
	/**
	 * shutdown imediate
	 */
	public void shutdownImediate()
	{
		server.shutdownImediate();
	}
}
