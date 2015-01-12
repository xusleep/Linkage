package service.middleware.linkage.framework.bootstrap;

import service.middleware.linkage.framework.handlers.EventDistributionMaster;
import service.middleware.linkage.framework.handlers.DefaultEventDistributionMaster;
import service.middleware.linkage.framework.io.WorkerPool;
import service.middleware.linkage.framework.io.nio.NIOWorkerPool;

public abstract class AbstractBootStrap implements BootStrap {
	// this is a client worker pool, this pool will handle all of the io operation 
	// with the server
	private final WorkerPool workPool;
	
	public AbstractBootStrap(EventDistributionMaster eventDistributionMaster){
		this.workPool = new NIOWorkerPool(eventDistributionMaster);
	}
	
	public WorkerPool getWorkerPool() {
		return workPool;
	}
}
