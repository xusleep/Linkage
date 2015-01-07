package service.middleware.linkage.framework.handlers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.event.ServiceEvent;

/**
 * this master will handle all of the event to thread pool for running
 * @author zhonxu
 *
 */
public class NIOFileEventDistributionMaster extends EventDistributionMaster {
	public  BlockingQueue<ServiceEvent> pool = new LinkedBlockingQueue<ServiceEvent>();
	private volatile boolean isShutdown = false;
	private static Logger  logger = Logger.getLogger(NIOFileEventDistributionMaster.class);
	private Handler firstHandler;
	private Handler lastHandler;

	public NIOFileEventDistributionMaster(){
	}
	
	/**
	 * register a handler to the master
	 * the handler will call from the first to last if you are willing 
	 * to transfer to next
	 * @param handler
	 */
	public void addHandler(Handler handler) {
		if(firstHandler == null)
		{
			lastHandler = firstHandler = handler;
		}
		else
		{
			lastHandler.setNext(handler);
			handler.setPrevious(lastHandler);
			lastHandler = handler;
		}
	}



	@Override
	public void run() {
		logger.debug("EventDistributionMaster start running ... hashCode = " + this.hashCode());
        while (true) {
            try 
            {
            	final ServiceEvent event = pool.take();
            	if(Thread.currentThread().isInterrupted() && isShutdown)
            	{
            		break;
            	}
            	// handle the event to thread poo
            	handleEvent(event);
            }
            catch (InterruptedException e) {
            	logger.debug("shut down invoke InterruptedException.");
            	if(pool.size() == 0 && isShutdown)
            	{
            		logger.debug("shut down by InterruptedException.");
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
							logger.error("not expected interruptedException happened. exception detail : " 
											+ StringUtils.ExceptionStackTraceToString(e1));
						}
            		}
            		break;
            	}
            	else
            	{
            		continue;
            	}
            }
        }
        
        logger.debug("EventDistributionMaster stop running ... hashCode = " + this.hashCode());
	}

	/**
	 * submit a event to the pool
	 */
	public void submitServiceEvent(ServiceEvent event) {
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
		logger.debug("shutdown master.");
		isShutdown = true;
		this.interrupt();
	}
	
	/**
	 * shutdown 
	 */
	public void shutdownImediate(){
		logger.debug("shutdown imediate master.");
		isShutdown = true;
		this.interrupt();
	}
	
	/**
	 * this method used to handle the event to the working pool
	 * 
	 * @param event
	 */
	private void handleEvent(final ServiceEvent event) {
		final Handler handler = firstHandler;
		try {
			handler.handleRequest(event);
		} catch (Exception e) {
			logger.error("not expected exception happened. exception detail : "
					+ StringUtils.ExceptionStackTraceToString(e));
		}
	}
}