package service.framework.monitor;

import service.framework.io.distribution.EventDistributionMaster;

public class MonitorThread extends Thread {
	private EventDistributionMaster eventDistributionHandler;
	
	public MonitorThread(EventDistributionMaster eventDistributionHandler) {
		// TODO Auto-generated constructor stub
		this.eventDistributionHandler = eventDistributionHandler;
	}

	@Override
	public void run() {
		while(true)
		{
			// TODO Auto-generated method stub
			try {
				Thread.sleep(1000);
				System.out.println("masterHandler.objExecutorService.toString() " + eventDistributionHandler.objExecutorService.toString());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
