package service.framework.io.server;

import service.framework.io.fire.MasterHandler;

public interface Client extends Runnable{
	public WorkerPool getWorkerPool();
	public MasterHandler getMasterHandler();
}
