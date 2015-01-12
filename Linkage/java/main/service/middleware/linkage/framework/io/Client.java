package service.middleware.linkage.framework.io;

import service.middleware.linkage.framework.handlers.EventDistributionMaster;

/**
 * client interface
 * @author zhonxu
 *
 */
public interface Client extends Runnable{
	public WorkerPool getWorkerPool();
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
