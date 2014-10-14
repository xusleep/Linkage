package service.framework.monitor;

import service.framework.io.fire.MasterHandler;
import service.framework.io.server.DefaultWorker;

public class MonitorThread extends Thread {

	@Override
	public void run() {
		while(true)
		{
			// TODO Auto-generated method stub
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
