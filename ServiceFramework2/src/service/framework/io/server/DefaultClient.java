package service.framework.io.server;

import service.framework.io.fire.MasterHandler;

public class DefaultClient implements Client {
	private final WorkerPool workerPool;
	private final MasterHandler objMasterHandler;

	public DefaultClient(MasterHandler objMasterHandler, WorkerPool workerPool){
		this.objMasterHandler = objMasterHandler;
		this.workerPool = workerPool;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		objMasterHandler.start();
		workerPool.start();
	}

	public WorkerPool getWorkerPool() {
		return workerPool;
	}

	public MasterHandler getMasterHandler() {
		return objMasterHandler;
	}

}
