package service.framework.monitor;

import service.framework.io.fire.MasterHandler;
import service.framework.io.server.DefaultWorker;

public class MonitorThread extends Thread {

	@Override
	public void run() {
		while(true)
		{
			// TODO Auto-generated method stub
			System.out.println("MasterHandler.pool.size()" + MasterHandler.pool.size());
			System.out.println("DefaultWorker.readBytesCount.get() = " + DefaultWorker.readBytesCount.get());
			System.out.println("DefaultWorker.writeBytesCount.get() = " + DefaultWorker.writeBytesCount.get());
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
