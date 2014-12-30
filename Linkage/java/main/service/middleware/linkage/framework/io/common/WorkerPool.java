package service.middleware.linkage.framework.io.common;

import java.nio.channels.SocketChannel;

/**
 * worker pool interface
 * @author zhonxu
 *
 */
public interface WorkerPool {
	public void start();
	public Worker getNextWorker();
	public WorkingChannel register(SocketChannel sc);
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
