package service.middleware.linkage.framework.io.client;

import service.middleware.linkage.framework.handlers.EventDistributionMaster;
import service.middleware.linkage.framework.io.common.WorkerPool;

/**
 * client interface
 * @author zhonxu
 *
 */
public interface Client extends Runnable{
	public WorkerPool getWorkerPool();
	public EventDistributionMaster getEventDistributionHandler();
	// wait for every thing ready
	public void waitReady();
	/**
	 * shutdown 
	 */
	public void shutdown();
	/**
	 * shutdown imediate
	 */
	public void shutdownImediate();
}
