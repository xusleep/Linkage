package service.framework.distribution;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import service.framework.common.StringUtils;
import service.framework.event.ServiceEvent;
import service.framework.handlers.Handler;

/**
 * this master will handle all of the event to thread pool for running
 * @author zhonxu
 *
 */
public class EventDistributionMaster extends Thread {
	public  BlockingQueue<ServiceEvent> pool = new LinkedBlockingQueue<ServiceEvent>();
	public final ExecutorService objExecutorService;
	private final List<Handler> eventHandlerList;
	private volatile boolean isShutdown = false;
	private final CountDownLatch shutdownSignal;
	private static Logger  logger = Logger.getLogger(EventDistributionMaster.class);  

	public EventDistributionMaster(int taskThreadPootSize){
		this.objExecutorService = Executors.newFixedThreadPool(taskThreadPootSize);
		this.eventHandlerList = new LinkedList<Handler>();
		shutdownSignal = new CountDownLatch(1);
	}
	
	/**
	 * register a handler to the master
	 * @param handler
	 */
	public void registerHandler(Handler handler) {
		this.eventHandlerList.add(handler);
	}



	@Override
	public void run() {
		logger.debug("EventDistributionMaster start running ... hashCode = " + this.hashCode());
		// TODO Auto-generated method stub
        while (true) {
            try 
            {
            	final ServiceEvent event = pool.take();
            	// handle the event to thread poo
            	handleEvent(event);
            }
            catch (InterruptedException e) {
            	if(pool.size() == 0 && isShutdown)
            	{
            		logger.debug("shut down.");
            		shutdownSignal.countDown();
            		break;
            	}
            	else if(pool.size() > 0 && isShutdown){
            		while(pool.size() > 0){
            			ServiceEvent event;
						try {
							event = pool.take();
	                    	// handle the event to thread pool
	                    	handleEvent(event);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							logger.error("not expected interruptedException happened. exception detail : " 
											+ StringUtils.ExceptionStackTraceToString(e1));
						}
            		}
            		shutdownSignal.countDown();
            		break;
            	}
            	else
            	{
            		continue;
            	}
            }
        }
	}

	/**
	 * submit a event to the pool
	 */
	public void submitEventPool(ServiceEvent event) {
		try {
			if(!isShutdown)
			{
				pool.put(event);
			}
			else
			{
				logger.error("drop event, because the master is being shutdown. event: " + event.toString());
			}
		} catch (InterruptedException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
		}
	}
	
	/**
	 * shutdown 
	 */
	public void shutdown(){
		Thread.currentThread().interrupt();
		isShutdown = true;
	}
	
	/**
	 * shutdown 
	 */
	public void shutdownImediate(){
		Thread.currentThread().interrupt();
		isShutdown = true;
		try {
			shutdownSignal.await();
		} catch (InterruptedException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
		}
	}
	
	/**
	 * this method used to handle the event to the working pool
	 * @param event
	 */
	private void handleEvent(final ServiceEvent event){
		this.objExecutorService.execute(new Runnable(){
			@Override
			public void run() {
            	try {
            		for(Handler handler : eventHandlerList)
            		{
            			handler.handleRequest(event);
            		}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("not expected exception happened. exception detail : " 
							+ StringUtils.ExceptionStackTraceToString(e));
				}
			}
    	});
	}
}
