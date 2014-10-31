package service.framework.io.server;

import service.framework.distribution.EventDistributionMaster;
import service.framework.io.common.WorkerPool;

public interface Server extends Runnable{
	public WorkerPool getWorkerPool();
	public EventDistributionMaster getMasterHandler();
	// wait for every thing ready
	public void waitReady();
}
