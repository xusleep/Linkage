package service.framework.setting;

/**
 * store the service information from properties
 * @author zhonxu
 *
 */
public class ServiceSettingEntity {
	private String serviceInterface;
	private String serviceName;
	private String serviceVersion;
	private String serviceTarget;
	private String serviceGroup;
	private Object serviceTargetObject;
	
	public String getServiceInterface() {
		return serviceInterface;
	}
	public void setServiceInterface(String serviceInterface) {
		this.serviceInterface = serviceInterface;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceVersion() {
		return serviceVersion;
	}
	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}
	public String getServiceTarget() {
		return serviceTarget;
	}
	public void setServiceTarget(String serviceTarget) {
		this.serviceTarget = serviceTarget;
	}
	public String getServiceGroup() {
		return serviceGroup;
	}
	public void setServiceGroup(String serviceGroup) {
		this.serviceGroup = serviceGroup;
	}
	public Object getServiceTargetObject() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if(serviceTargetObject == null)
		{
			synchronized(this){
				if(serviceTargetObject == null)
				{
					serviceTargetObject = Class.forName(this.getServiceTarget()).newInstance();
				}
			}
		}
		return serviceTargetObject;
	}	
}
