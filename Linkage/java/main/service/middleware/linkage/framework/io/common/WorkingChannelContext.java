package service.middleware.linkage.framework.io.common;

import java.nio.channels.Channel;


/**
 * hold the object when request a connect,
 * the system will be wrapped by.
 * @author zhonxu
 *
 */
public interface WorkingChannelContext extends ReadWrite {
	public Channel getChannel();
	/**
	 * get the current worker for this channel
	 * @return
	 */
	public Worker getWorker();
	public WorkingChannelStrategy findWorkingChannelStrategy(NIOWorkingMode workingMode);
	public void closeWorkingChannel();
}
