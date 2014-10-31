package service.framework.bootstrap;

import service.framework.comsume.ConsumerBean;
import service.framework.distribution.EventDistributionMaster;
import service.framework.handlers.ClientReadWriteHandler;
import service.framework.io.client.Client;
import service.framework.io.client.DefaultClient;
import service.framework.io.common.DefaultWorkerPool;
import service.framework.io.common.WorkerPool;
import service.framework.properties.WorkingPropertyEntity;
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
		this.eventDistributionHandler = new EventDistributionMaster(clientTaskThreadPootSize);
		this.workPool = new DefaultWorkerPool(this.eventDistributionHandler);
		this.client = new DefaultClient(eventDistributionHandler, this.workPool);
		WorkingPropertyEntity servicePropertyEntity = new WorkingPropertyEntity(propertyPath);
		//ServicePropertyEntity servicePropertyEntity = new ServicePropertyEntity("conf/client.properties");
		this.consumerBean = new ConsumerBean(servicePropertyEntity, this.workPool);
	}
	
	/**
	 * the user can use this cosumer bean to request service
	 * from the client
	 * @return
	 */
	public ConsumerBean getConsumerBean() {
		return consumerBean;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		client.getEventDistributionHandler().registerHandler(new ClientReadWriteHandler(this.getConsumerBean()));
		new Thread(client).start();
	}
}
