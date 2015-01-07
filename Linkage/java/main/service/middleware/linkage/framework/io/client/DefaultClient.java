package service.middleware.linkage.framework.io.client;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.common.WorkerPool;

/**
 * this class will be used in the client
 * a facade pattern used to capsulate all of the behavior 
 * @author zhonxu
 *
 */
public class DefaultClient implements Client {
	private final WorkerPool workerPool;
	private static Logger  logger = Logger.getLogger(DefaultClient.class); 

	public DefaultClient(WorkerPool workerPool){
		this.workerPool = workerPool;
	}
	
	@Override
	public void run() {
		logger.debug("start the client.");
		workerPool.start();
	}

	public WorkerPool getWorkerPool() {
		return workerPool;
	}

	@Override
	public void waitReady() {
		this.getWorkerPool().waitReady();
	}

	@Override
	public void shutdown() {
		logger.debug("shutdown the client.");
		workerPool.shutdown();
	}

	@Override
	public void shutdownImediate() {
		logger.debug("shutdown imediate the client.");
		this.getWorkerPool().shutdownImediate();
	}

}
