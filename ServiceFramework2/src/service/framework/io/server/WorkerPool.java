package service.framework.io.server;

import java.nio.channels.SocketChannel;

import service.framework.io.fire.MasterHandler;

public interface WorkerPool {
	public void start();
	public Worker getNextWorker();
	public WorkingChannel register(SocketChannel sc);
}
