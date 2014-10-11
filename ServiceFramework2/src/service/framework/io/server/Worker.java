package service.framework.io.server;

import java.nio.channels.SocketChannel;

public interface Worker extends Runnable{
	public void submitOpeRegister(SocketChannel sc);
}
