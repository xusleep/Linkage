package service.framework.monitor;

import service.framework.io.fire.MasterHandler;
import service.framework.io.server.DefaultWorker;

public class MonitorThread extends Thread {
	private MasterHandler masterHandler;
	
	public MonitorThread(MasterHandler masterHandler) {
		// TODO Auto-generated constructor stub
		this.masterHandler = masterHandler;
	}

	@Override
	public void run() {
		while(true)
		{
			// TODO Auto-generated method stub
			try {
				Thread.sleep(1000);
				System.out.println("masterHandler.objExecutorService.toString() " + masterHandler.objExecutorService.toString());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
