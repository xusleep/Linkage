package service.middleware.linkage.framework.bootstrap;

import service.middleware.linkage.framework.io.WorkerPool;

/**
 * this interface is used for the boot strap of the server or client 
 * @author zhonxu
 *
 */
public interface BootStrap {
	// get the worker pool
	public WorkerPool    getWorkerPool();
	// shutdown the boot strap
	public void shutdown();
	// shutdown the boot strap imediately
	public void shutdownImediate();
}
