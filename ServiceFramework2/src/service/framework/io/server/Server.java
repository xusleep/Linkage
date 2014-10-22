package service.framework.io.server;

import service.framework.io.fire.MasterHandler;

public interface Server extends Runnable{
	public WorkerPool getWorkerPool();
	public MasterHandler getMasterHandler();
	// wait for every thing ready
	public void waitReady();
}
