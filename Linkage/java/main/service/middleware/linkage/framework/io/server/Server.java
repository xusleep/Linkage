package service.middleware.linkage.framework.io.server;

import service.middleware.linkage.framework.io.common.WorkerPool;

/**
 * Server interface
 * @author zhonxu
 *
 */
public interface Server extends Runnable{
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
