package service.framework.provide.entity;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class RequestEntity implements Serializable {
	private String serviceName;
	private String methodName;
	private String version;
	private List args;
	private String group;
	
	public RequestEntity(){
		args = new LinkedList();
	}
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public List<String> getArgs() {
		return args;
	}
	public void setArgs(List<String> args) {
		this.args = args;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	
	
}
