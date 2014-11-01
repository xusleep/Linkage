package service.framework.bootstrap;

import service.framework.bootstrap.ServiceBootStrap;
import service.framework.distribution.EventDistributionMaster;
import service.framework.handlers.ServiceReadWriteHandler;
import service.framework.io.common.DefaultWorkerPool;
import service.framework.io.common.WorkerPool;
import service.framework.io.server.DefaultServer;
import service.framework.io.server.Server;
import service.framework.properties.WorkingPropertyEntity;
import service.framework.provide.ProviderBean;

public class ServiceCenterBootStrap implements Runnable{
	private Server server;
	private final ProviderBean providerBean;
	
	public ServiceCenterBootStrap(String propertyPath, int serviceTaskThreadPootSize){
		WorkingPropertyEntity objServicePropertyEntity = new WorkingPropertyEntity(propertyPath);
		EventDistributionMaster eventDistributionHandler = new EventDistributionMaster(5);
		WorkerPool workPool = new DefaultWorkerPool(eventDistributionHandler);
		this.providerBean = new ProviderBean(objServicePropertyEntity);
		eventDistributionHandler.registerHandler(new ServiceReadWriteHandler(providerBean));
		
		try {
			this.server = new DefaultServer(objServicePropertyEntity.getServiceAddress(), objServicePropertyEntity.getServicePort(),
					eventDistributionHandler, workPool);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop(){
		
	}

	public ProviderBean getProviderBean() {
		return providerBean;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		new Thread(server).start();
	}
	
}
