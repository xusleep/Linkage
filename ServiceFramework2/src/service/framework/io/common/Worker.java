package service.framework.io.common;

import java.nio.channels.SocketChannel;

public interface Worker extends Runnable{
	public WorkingChannel submitOpeRegister(SocketChannel sc);
	public boolean writeFromUser(WorkingChannel channel);
}
