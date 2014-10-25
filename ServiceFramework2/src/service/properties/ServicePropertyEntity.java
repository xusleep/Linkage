package service.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ServicePropertyEntity {
	
	private final String serviceAddress;
	private final int servicePort;
	private final List<ServiceEntity> serviceList = new LinkedList<ServiceEntity>();
	private final List<ServiceClientEntity> serviceClientList = new LinkedList<ServiceClientEntity>();
	
	public ServicePropertyEntity(String propertyFileName){
		InputStream inputStream = ServicePropertyEntity.class.getClassLoader().getResourceAsStream(propertyFileName);
		//创建一个Properties容器 
        Properties properties = new Properties(); 
        //从流中加载properties文件信息 
        try {
			properties.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        this.serviceAddress = properties.getProperty("serviceAddress");
        String strServicePort = properties.getProperty("servicePort");
        if(strServicePort != null && strServicePort != "")
        	this.servicePort = Integer.parseInt(strServicePort);
        else
        	this.servicePort = 0;
        for(int i = 1; i < 1000; i++)
        {
        	String serviceName = properties.getProperty("service.service" + i + ".name");
        	String clientName = properties.getProperty("client.service" + i + ".name");
        	if(serviceName != null && serviceName != ""){
        		ServiceEntity entity = new ServiceEntity();
            	entity.setServiceName(serviceName);
            	entity.setServiceGroup(properties.getProperty("service.service" + i + ".group"));
            	entity.setServiceInterface(properties.getProperty("service.service" + i + ".interface"));
            	entity.setServiceTarget(properties.getProperty("service.service" + i + ".target"));
            	entity.setServiceVersion(properties.getProperty("service.service" + i + ".version"));
            	serviceList.add(entity);
        		break;
        	}
        	if(clientName != null && clientName != ""){
        		ServiceClientEntity entity = new ServiceClientEntity();
            	entity.setServiceName(clientName);
            	entity.setServiceGroup(properties.getProperty("client.service" + i + ".group"));
            	entity.setServiceMethod(properties.getProperty("client.service" + i + ".method"));
            	entity.setServiceVersion(properties.getProperty("client.service" + i + ".version"));
            	entity.setId(properties.getProperty("client.service" + i + ".id"));
            	serviceClientList.add(entity);
        	}
        	if((serviceName == null || serviceName == "") && 
        			(clientName == null || clientName == "")){
        		break;
        	}
        }
	}
	
	
	
	public String getServiceAddress() {
		return serviceAddress;
	}



	public int getServicePort() {
		return servicePort;
	}



	public List<ServiceEntity> getServiceList() {
		return serviceList;
	}



	public List<ServiceClientEntity> getServiceClientList() {
		return serviceClientList;
	}

}
