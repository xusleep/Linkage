package servicecenter.service;

public class ServiceInformation {
	private String serviceName;
	private String serviceMethod;
	private String address;
	private String serviceVersion;
	private int port;
	
	public ServiceInformation(){
	}
	
	public ServiceInformation(String address, int port, String serviceName, String serviceMethod, String serviceVersion)
	{
		this.address = address;
		this.port = port;
		this.serviceName = serviceName;
		this.serviceMethod = serviceMethod;
		this.serviceVersion = serviceVersion;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getServiceMethod() {
		return serviceMethod;
	}
	
	public void setServiceMethod(String serviceMethod) {
		this.serviceMethod = serviceMethod;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}
	
}
