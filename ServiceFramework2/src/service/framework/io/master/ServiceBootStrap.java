package service.framework.io.master;

import service.framework.io.client.comsume.ConsumerBean;
import service.framework.io.distribution.EventDistributionMaster;
import service.framework.io.handlers.ClientReadWriteHandler;
import service.framework.io.handlers.ReadWriteHandler;
import service.framework.io.handlers.ServiceRegisterHandler;
import service.framework.io.server.Client;
import service.framework.io.server.DefaultClient;
import service.framework.io.server.DefaultServer;
import service.framework.io.server.DefaultWorkerPool;
import service.framework.io.server.Server;
import service.framework.io.server.WorkerPool;
import service.framework.provide.ProviderBean;
import service.properties.ServicePropertyEntity;

public class ServiceBootStrap implements Runnable {
	private final Client client;
	private final Server server;
	private final ProviderBean providerBean;
	private final ConsumerBean consumerBean;
	
	public ServiceBootStrap(String propertyPath, int serviceTaskThreadPootSize, int clientTaskThreadPootSize) throws Exception{
		// read the configuration from the properties
		ServicePropertyEntity objServicePropertyEntity = new ServicePropertyEntity(propertyPath);
		// new a task handler, this handler will handle all of the task from the pool queue
		// into the executor pool(thread pool) which will execute the task.
		EventDistributionMaster eventDistributionHandler = new EventDistributionMaster(serviceTaskThreadPootSize);
		// this worker pool will handle the read write io operation for all of the connection
		WorkerPool workerPool = new DefaultWorkerPool(eventDistributionHandler);
		// this is a provider which provides the service point access from the io layer
		// in this provider, all of the service information will load into the bean
		// when there is a request, the provider will find the service, init it & execute the service
		this.providerBean = new ProviderBean(objServicePropertyEntity);
		// this is a handler for the service, which will read the requestion information & call the provider 
		// to handle further
		eventDistributionHandler.registerHandler(new ReadWriteHandler(providerBean));
		
		// this is the server, it will accept all of the connection & register the channel into the worker pool
		this.server = new DefaultServer(objServicePropertyEntity.getServiceAddress(), objServicePropertyEntity.getServicePort(),
				eventDistributionHandler, workerPool);
		
		// new a task handler, this handler will handle all of the task from the pool queue
		// into the executor pool(thread pool) which will execute the task.
		EventDistributionMaster clientEventDistributionHandler = new EventDistributionMaster(clientTaskThreadPootSize);
		// this is a client worker pool, this pool will handle all of the io operation 
		// with the server
		WorkerPool clientWorkerPool = new DefaultWorkerPool(clientEventDistributionHandler);
		// this is a client, in this client it will be a gather place where we will start the worker pool & task handler 
		this.client = new DefaultClient(clientEventDistributionHandler, clientWorkerPool);
		this.consumerBean = new ConsumerBean(objServicePropertyEntity, clientWorkerPool);
		eventDistributionHandler.registerHandler(new ServiceRegisterHandler(this.consumerBean));
	}
	
	public Client getClient() {
		return client;
	}

	public void stop(){
		
	}
	
    public ConsumerBean getConsumerBean() {
		return consumerBean;
	}

	public ProviderBean getProviderBean() {
		return providerBean;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		new Thread(server).start();
		client.getEventDistributionHandler().registerHandler(new ClientReadWriteHandler(this.getConsumerBean()));
		new Thread(client).start();
	}
}
