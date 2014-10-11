package service.framework.io.server.statistics;

public class StatisticsInformation {
	private final static int NOT_AVAILABLE = 3;
	private final static int BUSY = 2;
	private final static int NORMAL = 1;
	private final static int FREE = 0 ;
	private String serviceAddress;
	private int servicePort;
	private int eventQueueCount;
	private int status;
	
	
	public String getServiceAddress() {
		return serviceAddress;
	}



	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}



	public int getServicePort() {
		return servicePort;
	}



	public void setServicePort(int servicePort) {
		this.servicePort = servicePort;
	}



	public int getEventQueueCount() {
		return eventQueueCount;
	}



	public void setEventQueueCount(int eventQueueCount) {
		this.eventQueueCount = eventQueueCount;
	}



	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	
	
}
