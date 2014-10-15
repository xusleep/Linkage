package service.framework.io.server;

import java.nio.channels.SocketChannel;

import service.framework.io.fire.MasterHandler;

public class DefaultWorkerPool implements WorkerPool {
	private final int WORKERCOUNTER = Runtime.getRuntime().availableProcessors();
	private Worker[] workers = new Worker[WORKERCOUNTER];
	private int nextWorkCount = 0;
	private MasterHandler objMasterHandler;
	
	public DefaultWorkerPool(MasterHandler objMasterHandler){
		for(int i = 0; i < workers.length; i++){
			try {
				workers[i] = new DefaultWorker(objMasterHandler);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void start(){
		for(int i = 0; i < workers.length; i++){
			new Thread(workers[i]).start();
		}
	}
	
	public Worker getNextWorker(){
		return workers[Math.abs(nextWorkCount++)%WORKERCOUNTER];
	}
	
	public WorkingChannel register(SocketChannel sc){
		Worker worker = getNextWorker();
		return worker.submitOpeRegister(sc);
	}
}
