package service.middleware.framework.io.client;

import service.middleware.framework.distribution.EventDistributionMaster;
import service.middleware.framework.io.common.WorkerPool;

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
