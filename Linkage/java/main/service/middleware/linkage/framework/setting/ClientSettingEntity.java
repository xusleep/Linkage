package service.middleware.linkage.framework.setting;

/**
 * store the client information from properties
 * @author zhonxu
 *
 */
public class ClientSettingEntity {
	private String serviceName;
	private String serviceMethod;
	private String serviceVersion;
	private String serviceGroup;
	private String id;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getServiceGroup() {
		return serviceGroup;
	}
	public void setServiceGroup(String serviceGroup) {
		this.serviceGroup = serviceGroup;
	}
}
