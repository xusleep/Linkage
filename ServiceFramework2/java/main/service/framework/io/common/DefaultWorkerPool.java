package service.framework.io.common;

import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import service.framework.common.StringUtils;
import service.framework.distribution.EventDistributionMaster;

/**
 * Worker pool, put the channel into the worker pool
 * the worker deal with the bussiness of receiving message & send message
 * @author zhonxu
 *
 */
public class DefaultWorkerPool implements WorkerPool {
	private Worker[] workers;
	private int nextWorkCount = 0;
	private final CountDownLatch signal;
	private static Logger  logger = Logger.getLogger(DefaultWorkerPool.class); 
	
	/**
	 * 
	 * @param eventDistributionHandler
	 * @param workerCounter assign the point
	 */
	public DefaultWorkerPool(EventDistributionMaster eventDistributionHandler, int workerCounter){
		signal = new CountDownLatch(workerCounter);
		workers = new Worker[workerCounter];
		for(int i = 0; i < workers.length; i++){
			try {
				workers[i] = new DefaultWorker(eventDistributionHandler, signal);
			} catch (Exception e) {
				logger.error("not expected interruptedException happened. exception detail : " 
						+ StringUtils.ExceptionStackTraceToString(e));
			}
		}
	}
	
	public DefaultWorkerPool(EventDistributionMaster eventDistributionHandler){
		int workerCounter = Runtime.getRuntime().availableProcessors();
		signal = new CountDownLatch(workerCounter);
		workers = new Worker[workerCounter];
		for(int i = 0; i < workers.length; i++){
			try {
				workers[i] = new DefaultWorker(eventDistributionHandler, signal);
			} catch (Exception e) {
				logger.error("not expected interruptedException happened. exception detail : " 
						+ StringUtils.ExceptionStackTraceToString(e));
			}
		}
	}
	
	public void start(){
		logger.debug("starting all of  the worker.");
		for(int i = 0; i < workers.length; i++){
			new Thread(workers[i]).start();
		}
	}
	
	public Worker getNextWorker(){
		return workers[Math.abs(nextWorkCount++)%workers.length];
	}
	
	public WorkingChannel register(SocketChannel sc){
		Worker worker = getNextWorker();
		return worker.submitOpeRegister(sc);
	}

	@Override
	public void waitReady() {
		try {
			signal.await();
		} catch (InterruptedException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
		}
	}

	@Override
	public void shutdown() {
		logger.debug("shutdown all of the workers.");
		// TODO Auto-generated method stub
		for(int i = 0; i < workers.length; i++){
			workers[i].shutdown();
		}
	}

	@Override
	public void shutdownImediate() {
		logger.debug("shutdown imediate all of the workers.");
		// TODO Auto-generated method stub
		for(int i = 0; i < workers.length; i++){
			workers[i].shutdownImediate();
		}
	}
}
