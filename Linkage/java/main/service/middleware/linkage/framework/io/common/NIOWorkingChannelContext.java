package service.middleware.linkage.framework.io.common;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

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
	private volatile WorkingChannelMode readWorkingMode = WorkingChannelMode.MessageMode;
	private volatile WorkingChannelMode writeWorkingMode = WorkingChannelMode.MessageMode;
	private List<WorkingChannelStrategy> workingChannelStrategyList = new LinkedList<WorkingChannelStrategy>();
	private static Logger  logger = Logger.getLogger(NIOWorkingChannelContext.class);
	
	public NIOWorkingChannelContext(Channel channel, Worker worker, EventDistributionMaster eventDistributionHandler){
		this.channel = channel;
		this.worker = worker;
		workingChannelStrategyList.add(new NIOMessageWorkingChannelStrategy(this, eventDistributionHandler));
		workingChannelStrategyList.add(new NIOFileWorkingChannelStrategy());
	}
	
	@Override
	public WorkingChannelStrategy findWorkingChannelStrategy(WorkingChannelMode workingMode) {
		for(WorkingChannelStrategy workingChannelStrategy : workingChannelStrategyList){
			if(workingMode == WorkingChannelMode.MessageMode 
					&& workingChannelStrategy instanceof NIOMessageWorkingChannelStrategy){
				return workingChannelStrategy;
			}
			else if(workingMode == WorkingChannelMode.FileMode 
					&& workingChannelStrategy instanceof NIOFileWorkingChannelStrategy){
				return workingChannelStrategy;
			}
		}
		return null;
	}
	
	@Override
	public WorkingChannelOperationResult read() {
		return findWorkingChannelStrategy(readWorkingMode).read();
	}

	@Override
	public WorkingChannelOperationResult write() {
		return findWorkingChannelStrategy(writeWorkingMode).write();
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
	
	public WorkingChannelMode getReadWorkingMode() {
		return readWorkingMode;
	}

	public void setReadWorkingMode(WorkingChannelMode workingMode) {
		this.readWorkingMode = workingMode;
	}

	public WorkingChannelMode getWriteWorkingMode() {
		return writeWorkingMode;
	}

	public void setWriteWorkingMode(WorkingChannelMode writeWorkingMode) {
		this.writeWorkingMode = writeWorkingMode;
	}
}