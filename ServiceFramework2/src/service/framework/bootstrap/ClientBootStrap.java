package service.framework.bootstrap;

import service.framework.comsume.ConsumerBean;
import service.framework.distribution.EventDistributionMaster;
import service.framework.handlers.ClientReadWriteHandler;
import service.framework.io.client.Client;
import service.framework.io.client.DefaultClient;
import service.framework.io.common.DefaultWorkerPool;
import service.framework.io.common.WorkerPool;
import service.framework.properties.WorkingPropertyEntity;

public class ClientBootStrap implements Runnable {
	private final Client client;
	private final EventDistributionMaster eventDistributionHandler;
	private final WorkerPool workPool;
	private final ConsumerBean consumerBean;
	
	public ClientBootStrap(String propertyPath, int clientTaskThreadPootSize){
		this.eventDistributionHandler = new EventDistributionMaster(clientTaskThreadPootSize);
		this.workPool = new DefaultWorkerPool(this.eventDistributionHandler);
		this.client = new DefaultClient(eventDistributionHandler, this.workPool);
		WorkingPropertyEntity servicePropertyEntity = new WorkingPropertyEntity(propertyPath);
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
