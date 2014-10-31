package service.framework.io.server;

import java.nio.channels.SocketChannel;

public interface WorkerPool {
	public void start();
	public Worker getNextWorker();
	public WorkingChannel register(SocketChannel sc);
	// wait for every thing ready
	public void waitReady();
}
