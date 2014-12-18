package service.framework.io.client;

import org.apache.log4j.Logger;

import service.framework.distribution.EventDistributionMaster;
import service.framework.io.common.WorkerPool;

/**
 * this class will be used in the client
 * a facade pattern used to capsulate all of the behavior 
 * @author zhonxu
 *
 */
public class DefaultClient implements Client {
	private final WorkerPool workerPool;
	private final EventDistributionMaster eventDistributionHandler;
	private static Logger  logger = Logger.getLogger(DefaultClient.class); 

	public DefaultClient(EventDistributionMaster objMasterHandler, WorkerPool workerPool){
		this.eventDistributionHandler = objMasterHandler;
		this.workerPool = workerPool;
	}
	
	@Override
	public void run() {
		logger.debug("start the client.");
		// TODO Auto-generated method stub
		eventDistributionHandler.start();
		workerPool.start();
	}

	public WorkerPool getWorkerPool() {
		return workerPool;
	}

	public EventDistributionMaster getEventDistributionHandler() {
		return eventDistributionHandler;
	}

	@Override
	public void waitReady() {
		this.getWorkerPool().waitReady();
	}

	@Override
	public void shutdown() {
		logger.debug("shutdown the client.");
		workerPool.shutdown();
		eventDistributionHandler.shutdown();
	}

	@Override
	public void shutdownImediate() {
		logger.debug("shutdown imediate the client.");
		this.getWorkerPool().shutdownImediate();
		eventDistributionHandler.shutdownImediate();
	}

}
