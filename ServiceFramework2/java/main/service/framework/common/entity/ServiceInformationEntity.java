package service.framework.common.entity;

/**
 * this class use to hold the information of a service
 * @author zhonxu
 *
 */
public class ServiceInformationEntity {
	private String serviceName;
	private String serviceMethod;
	private String address;
	private String serviceVersion;
	private int    port;
	
	public ServiceInformationEntity(){
	}
	
	public ServiceInformationEntity(String address, int port, String serviceName, String serviceMethod, String serviceVersion)
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

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.address + "_" +  this.port + "_UNIQUE_STRING";
	}
	
	
}
