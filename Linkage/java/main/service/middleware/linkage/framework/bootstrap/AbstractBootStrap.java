package service.middleware.linkage.framework.bootstrap;

import service.middleware.linkage.framework.distribution.EventDistributionMaster;
import service.middleware.linkage.framework.io.common.NIOWorkerPool;
import service.middleware.linkage.framework.io.common.WorkerPool;

public abstract class AbstractBootStrap implements BootStrap {
	// new a task handler, this handler will handle all of the task from the pool queue
	// into the executor pool(thread pool) which will execute the task.
	private final EventDistributionMaster eventDistributionHandler;
	// this is a client worker pool, this pool will handle all of the io operation 
	// with the server
	private final WorkerPool workPool;
	
	public AbstractBootStrap(int taskThreadPootSize){
		eventDistributionHandler = new EventDistributionMaster(taskThreadPootSize);
		this.workPool = new NIOWorkerPool(eventDistributionHandler, 1);
	}
	
	public WorkerPool getWorkerPool() {
		return workPool;
	}

	public EventDistributionMaster getEventDistributionHandler() {
		return eventDistributionHandler;
	}
}
