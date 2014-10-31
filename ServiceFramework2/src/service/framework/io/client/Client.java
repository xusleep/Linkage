package service.framework.io.client;

import service.framework.distribution.EventDistributionMaster;
import service.framework.io.common.WorkerPool;

public interface Client extends Runnable{
	public WorkerPool getWorkerPool();
	public EventDistributionMaster getEventDistributionHandler();
	// wait for every thing ready
	public void waitReady();
}
