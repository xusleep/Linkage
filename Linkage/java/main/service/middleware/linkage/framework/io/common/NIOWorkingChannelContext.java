package service.middleware.linkage.framework.io.common;

import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.framework.distribution.EventDistributionMaster;

/**
 * hold the object when request a connect,
 * the system will be wrapped by.
 * @author zhonxu
 *
 */
public class NIOWorkingChannelContext implements WorkingChannelContext {

    /**
     * Monitor object for write.
     */
    final public Object writeLock = new Object();
    /**
     * Monitor object for read.
     */
    final public Object readLock = new Object();
	private Channel channel;
	private Worker worker;
	private SelectionKey key;
	private String workingChannelCacheID;
	private volatile NIOWorkingMode workingMode = NIOWorkingMode.MessageMode;
	private List<WorkingChannelStrategy> workingChannelStrategyList = new LinkedList<WorkingChannelStrategy>();
	
	public NIOWorkingChannelContext(Channel channel, Worker worker, EventDistributionMaster eventDistributionHandler){
		this.channel = channel;
		this.worker = worker;
		workingChannelStrategyList.add(new NIOWorkingChannelMessageStrategy(this, eventDistributionHandler));
		workingChannelStrategyList.add(new NIOWorkingChannelFileStrategy());
	}
	
	@Override
	public WorkingChannelStrategy getWorkingChannelStrategy() {
		for(WorkingChannelStrategy workingChannelStrategy : workingChannelStrategyList){
			if(workingMode == NIOWorkingMode.MessageMode 
					&& workingChannelStrategy instanceof NIOWorkingChannelMessageStrategy){
				return workingChannelStrategy;
			}
			else if(workingMode == NIOWorkingMode.FileMode 
					&& workingChannelStrategy instanceof NIOWorkingChannelFileStrategy){
				return workingChannelStrategy;
			}
		}
		return null;
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
	
	public NIOWorkingMode getWorkingMode() {
		return workingMode;
	}

	public void setWorkingMode(NIOWorkingMode workingMode) {
		this.workingMode = workingMode;
	}
}
