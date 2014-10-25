package service.framework.io.master;

import service.framework.io.fire.MasterHandler;
import service.framework.io.handlers.ReadWriteHandler;
import service.framework.io.server.Client;
import service.framework.io.server.DefaultServer;
import service.framework.io.server.DefaultWorkerPool;
import service.framework.io.server.Server;
import service.framework.io.server.WorkerPool;
import service.framework.monitor.MonitorThread;
import service.framework.provide.ProviderBean;
import service.properties.ServicePropertyEntity;

public class ServiceBootStrap {
	private Client client;
	private static ServiceBootStrap instance = new ServiceBootStrap();
	private final MasterHandler masterHandler;
	private final WorkerPool workPool;
	private Server server;
	private final ProviderBean providerBean;
	
	private ServiceBootStrap(){
		ServicePropertyEntity objServicePropertyEntity = new ServicePropertyEntity("conf/service.properties");
		this.masterHandler = new MasterHandler(5);
		this.workPool = new DefaultWorkerPool(this.masterHandler);
		this.providerBean = new ProviderBean(objServicePropertyEntity.getServiceList());
		this.masterHandler.registerHandler(new ReadWriteHandler(providerBean));
		try {
			this.server = new DefaultServer(objServicePropertyEntity.getServiceAddress(), objServicePropertyEntity.getServicePort(),
					this.masterHandler, this.workPool);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       // this.client = (Client)this.applicationContext.getBean("defaultClient");
	}
	
	public static ServiceBootStrap getInstance() {
		return instance;
	}
	
	public void start(){
		//masterHandler.registerHandler(new ServiceRegisterHandler(applicationContext));
		new Thread(server).start();
		new MonitorThread(this.masterHandler).start();
		//client.getMasterHandler().registerHandler(new ClientReadWriteHandler());
		//new Thread(client).start();
	}
	
	public Client getClient() {
		return client;
	}

	public void stop(){
		
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
