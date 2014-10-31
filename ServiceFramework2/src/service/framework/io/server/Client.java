package service.framework.io.server;

import service.framework.distribution.EventDistributionMaster;

public interface Client extends Runnable{
	public WorkerPool getWorkerPool();
	public EventDistributionMaster getEventDistributionHandler();
	// wait for every thing ready
	public void waitReady();
}
