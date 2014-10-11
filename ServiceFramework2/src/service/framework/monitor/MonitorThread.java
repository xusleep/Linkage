package service.framework.monitor;

import service.framework.io.fire.MasterHandler;

public class MonitorThread extends Thread {

	@Override
	public void run() {
		while(true)
		{
			// TODO Auto-generated method stub
			System.out.println("MasterHandler.pool.size()" + MasterHandler.pool.size());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
