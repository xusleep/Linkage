package service.middleware.linkage.framework.io;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.handlers.EventDistributionMaster;
import service.middleware.linkage.framework.io.nio.NIOWorkingChannelContext;

/**
 * the interface of the the working channel strategy
 * @author zhonxu
 *
 */
public abstract class WorkingChannelStrategy implements WorkingChannelReadWrite{
	private final NIOWorkingChannelContext workingChannelContext;
	private static Logger  logger = Logger.getLogger(WorkingChannelStrategy.class);
	
	public WorkingChannelStrategy(NIOWorkingChannelContext workingChannelContext){
		this.workingChannelContext = workingChannelContext;

	}
	
	public NIOWorkingChannelContext getWorkingChannelContext() {
		return workingChannelContext;
	}
}
