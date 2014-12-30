package service.middleware.framework.io.common;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * worker interface
 * @author zhonxu
 *
 */
public interface Worker extends Runnable{
	public WorkingChannel submitOpeRegister(SocketChannel sc);
	public boolean writeFromUser(WorkingChannel channel);
	public void closeWorkingChannel(WorkingChannel workingchannel) throws IOException;
	/**
	 * shutdown 
	 */
	public void shutdown();
	/**
	 * shutdown imediate
	 */
	public void shutdownImediate();
}
