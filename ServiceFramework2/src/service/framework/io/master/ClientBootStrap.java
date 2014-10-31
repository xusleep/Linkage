package service.framework.io.master;

import service.framework.io.client.comsume.ConsumerBean;
import service.framework.io.distribution.EventDistributionMaster;
import service.framework.io.handlers.ClientReadWriteHandler;
import service.framework.io.server.Client;
import service.framework.io.server.DefaultClient;
import service.framework.io.server.DefaultWorkerPool;
import service.framework.io.server.WorkerPool;
import service.properties.ServicePropertyEntity;

public class ClientBootStrap implements Runnable {
	private final Client client;
	private final EventDistributionMaster eventDistributionHandler;
	private final WorkerPool workPool;
	private final ConsumerBean consumerBean;
	
	public ClientBootStrap(String propertyPath, int clientTaskThreadPootSize){
		this.eventDistributionHandler = new EventDistributionMaster(clientTaskThreadPootSize);
		this.workPool = new DefaultWorkerPool(this.eventDistributionHandler);
		this.client = new DefaultClient(eventDistributionHandler, this.workPool);
		ServicePropertyEntity servicePropertyEntity = new ServicePropertyEntity(propertyPath);
		//ServicePropertyEntity servicePropertyEntity = new ServicePropertyEntity("conf/client.properties");
		this.consumerBean = new ConsumerBean(servicePropertyEntity, this.workPool);
	}
	
	public ConsumerBean getConsumerBean() {
		return consumerBean;
	}
	
	public WorkerPool getWorkPool() {
		return workPool;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		//new MonitorThread(client.getMasterHandler()).start();
		client.getEventDistributionHandler().registerHandler(new ClientReadWriteHandler(this.getConsumerBean()));
		new Thread(client).start();
	}
}
