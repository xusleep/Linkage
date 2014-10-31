package service.framework.io.server;

import service.framework.io.distribution.EventDistributionMaster;

public class DefaultClient implements Client {
	private final WorkerPool workerPool;
	private final EventDistributionMaster eventDistributionHandler;

	public DefaultClient(EventDistributionMaster objMasterHandler, WorkerPool workerPool){
		this.eventDistributionHandler = objMasterHandler;
		this.workerPool = workerPool;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		eventDistributionHandler.start();
		workerPool.start();
	}

	public WorkerPool getWorkerPool() {
		return workerPool;
	}

	public EventDistributionMaster getEventDistributionHandler() {
		return eventDistributionHandler;
	}

	@Override
	public void waitReady() {
		// TODO Auto-generated method stub
		this.getWorkerPool().waitReady();
	}

}
