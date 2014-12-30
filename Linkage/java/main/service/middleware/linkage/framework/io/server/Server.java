package service.middleware.linkage.framework.io.server;

import service.middleware.linkage.framework.distribution.EventDistributionMaster;
import service.middleware.linkage.framework.io.common.WorkerPool;

/**
 * Server interface
 * @author zhonxu
 *
 */
public interface Server extends Runnable{
	public WorkerPool getWorkerPool();
	public EventDistributionMaster getMasterHandler();
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
