package service.framework.io.master;

import java.io.IOException;

import service.framework.io.client.comsume.ConsumerBean;
import service.framework.io.fire.MasterHandler;
import service.framework.io.handlers.ClientReadWriteHandler;
import service.framework.io.server.Client;
import service.framework.io.server.DefaultClient;
import service.framework.io.server.DefaultWorkerPool;
import service.framework.io.server.WorkerPool;
import service.framework.route.DefaultRoute;
import service.properties.ServicePropertyEntity;
import servicecenter.service.ServiceInformation;

public class ClientBootStrap {
	private final Client client;
	private static ClientBootStrap instance = new ClientBootStrap();
	private final MasterHandler masterHandler;
	private final WorkerPool workPool;
	private final ConsumerBean consumerBean;
	private ClientBootStrap(){
		this.masterHandler = new MasterHandler(5);
		this.workPool = new DefaultWorkerPool(this.masterHandler);
		this.client = new DefaultClient(this.masterHandler, this.workPool);
		ServicePropertyEntity servicePropertyEntity = new ServicePropertyEntity("conf/client.properties");
		DefaultRoute objDefaultRoute = new DefaultRoute();
		ServiceInformation objServiceInformation = new ServiceInformation("localhost", 5001, "", "", "");
		objDefaultRoute.getServiceList().add(objServiceInformation);
		this.consumerBean = new ConsumerBean(objDefaultRoute, servicePropertyEntity.getServiceClientList(), this.workPool);
	}
	
	public ConsumerBean getConsumerBean() {
		return consumerBean;
	}

	public static ClientBootStrap getInstance(){
		return instance;
	}
	

	public WorkerPool getWorkPool() {
		return workPool;
	}

	public void start() throws IOException {
		//new MonitorThread(client.getMasterHandler()).start();
		client.getMasterHandler().registerHandler(new ClientReadWriteHandler(this.getConsumerBean()));
		new Thread(client).start();
	}
	
	public static void main(String[] args) throws IOException {
		ClientBootStrap.getInstance().start();
	}
}
