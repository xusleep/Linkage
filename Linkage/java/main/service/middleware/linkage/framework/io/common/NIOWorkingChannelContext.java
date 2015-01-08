package service.middleware.linkage.framework.io.common;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.handlers.EventDistributionMaster;

/**
 * hold the object when request a connect,
 * the system will be wrapped by.
 * @author zhonxu
 *
 */
public class NIOWorkingChannelContext implements WorkingChannelContext {
	private Channel channel;
	private Worker worker;
	private SelectionKey key;
	private String workingChannelCacheID;
	private volatile WorkingChannelMode workingMode;
	private WorkingChannelStrategy workingChannelStrategy;
	private static Logger  logger = Logger.getLogger(NIOWorkingChannelContext.class);
	private final EventDistributionMaster eventDistributionHandler;
	
	public NIOWorkingChannelContext(Channel channel, WorkingChannelMode workingMode, Worker worker, EventDistributionMaster eventDistributionHandler){
		this.channel = channel;
		this.worker = worker;
		this.eventDistributionHandler = eventDistributionHandler;
		switchWorkMode(workingMode);
	}
	
	public void switchWorkMode(WorkingChannelMode theWorkingMode){
		this.workingMode = theWorkingMode;
		if(this.workingMode == WorkingChannelMode.MESSAGEMODE )
		{
			if(this.workingChannelStrategy != null){
				this.workingChannelStrategy.clear();
			}
			this.workingChannelStrategy = new NIOMessageWorkingChannelStrategy(this, eventDistributionHandler);
			logger.debug("working channel mode is changed : " + theWorkingMode);
		}
		else if(this.workingMode == WorkingChannelMode.FILEMODE )
		{
			if(this.workingChannelStrategy != null){
				this.workingChannelStrategy.clear();
			}
			this.workingChannelStrategy = new NIOFileWorkingChannelStrategy(this,  eventDistributionHandler);
			logger.debug("working channel mode is changed : " + theWorkingMode);
		}
	}
	
	public WorkingChannelStrategy getWorkingChannelStrategy() {
		return this.workingChannelStrategy ;
	}
	
	@Override
	public WorkingChannelOperationResult readChannel() {
		return getWorkingChannelStrategy().readChannel();
	}

	@Override
	public WorkingChannelOperationResult writeChannel() {
		return getWorkingChannelStrategy().writeChannel();
	}
	
	/**
	 * close the channel
	 * 
	 * @param sc
	 * @throws IOException
	 */
	public void closeWorkingChannel() {
		key.cancel();
		SocketChannel sc = (SocketChannel)key.channel();
		try {
			sc.close();
		} catch (IOException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
		}
	}

	public Worker getWorker() {
		return worker;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
    public SelectionKey getKey() {
		return key;
	}

	public void setKey(SelectionKey key) {
		this.key = key;
	}
	
	public String getWoringChannelCacheID() {
		return workingChannelCacheID;
	}

	public void setWorkingChannelCacheID(String workingChannelCacheID) {
		this.workingChannelCacheID = workingChannelCacheID;
	}

	public WorkingChannelMode getWorkingChannelMode() {
		return workingMode;
	}
	
	
}
