package service.middleware.linkage.framework.handlers;


public abstract class EventDistributionMaster extends Thread{
	/**
	 * register a handler to the master
	 * the handler will call from the first to last if you are willing 
	 * to transfer to next
	 * @param handler
	 */
	public abstract void addHandler(Handler handler);
	/**
	 * submit a event to the pool
	 */
	public abstract void submitServiceEvent(ServiceEvent event);
	/**
	 * shutdown 
	 */
	public abstract void shutdown();
	
	/**
	 * shutdown 
	 */
	public abstract void shutdownImediate();
}
