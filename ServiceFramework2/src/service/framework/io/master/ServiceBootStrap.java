package service.framework.io.master;

import service.framework.io.client.comsume.ConsumerBean;
import service.framework.io.fire.MasterHandler;
import service.framework.io.handlers.ClientReadWriteHandler;
import service.framework.io.handlers.ReadWriteHandler;
import service.framework.io.handlers.ServiceRegisterHandler;
import service.framework.io.server.Client;
import service.framework.io.server.DefaultClient;
import service.framework.io.server.DefaultServer;
import service.framework.io.server.DefaultWorkerPool;
import service.framework.io.server.Server;
import service.framework.io.server.WorkerPool;
import service.framework.monitor.MonitorThread;
import service.framework.provide.ProviderBean;
import service.framework.route.DefaultRoute;
import service.properties.ServicePropertyEntity;
import servicecenter.service.ServiceInformation;

public class ServiceBootStrap {
	private Client client;
	private static ServiceBootStrap instance = new ServiceBootStrap();
	private Server server;
	private final ProviderBean providerBean;
	private final ConsumerBean consumerBean;
	
	private ServiceBootStrap(){
		
		ServicePropertyEntity objServicePropertyEntity = new ServicePropertyEntity("conf/service1.properties");
		MasterHandler masterHandler = new MasterHandler(5);
		WorkerPool workPool = new DefaultWorkerPool(masterHandler);
		this.providerBean = new ProviderBean(objServicePropertyEntity);
		masterHandler.registerHandler(new ReadWriteHandler(providerBean));
		
		try {
			this.server = new DefaultServer(objServicePropertyEntity.getServiceAddress(), objServicePropertyEntity.getServicePort(),
					masterHandler, workPool);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		MasterHandler clientmasterHandler = new MasterHandler(5);
		WorkerPool clientWorkPool = new DefaultWorkerPool(clientmasterHandler);
		this.client = new DefaultClient(clientmasterHandler, clientWorkPool);
		this.consumerBean = new ConsumerBean(objServicePropertyEntity, clientWorkPool);
	
		masterHandler.registerHandler(new ServiceRegisterHandler(this.consumerBean));
	}
	
	public static ServiceBootStrap getInstance() {
		return instance;
	}
	
	public void start(){
		new Thread(server).start();
		client.getMasterHandler().registerHandler(new ClientReadWriteHandler(this.getConsumerBean()));
		new Thread(client).start();
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

	public static void main(String[] args) {
        try {
        	ServiceBootStrap.getInstance().start();
        }
        catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
            System.exit(-1);
        }
    }
}
