package service.framework.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import static service.framework.common.ShareingData.*;

import service.framework.common.ShareingData;

public class WorkingPropertyEntity {
	
	private final String serviceAddress;
	private final int servicePort;
	private final String serviceCenterAddress;
	private final int serviceCenterPort;
	private final List<ServicePropertyEntity> serviceList = new LinkedList<ServicePropertyEntity>();
	private final List<ClientPropertyEntity> serviceClientList = new LinkedList<ClientPropertyEntity>();
	
	public WorkingPropertyEntity(String propertyFileName){
		InputStream inputStream = WorkingPropertyEntity.class.getClassLoader().getResourceAsStream(propertyFileName);
		//创建一个Properties容器 
        Properties properties = new Properties(); 
        //从流中加载properties文件信息 
        try {
			properties.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        this.serviceAddress = properties.getProperty(SERVICE_ADDRESS);
        String strServicePort = properties.getProperty(SERVICE_PORT);
        if(strServicePort != null && strServicePort != "")
        	this.servicePort = Integer.parseInt(strServicePort);
        else
        	this.servicePort = 0;
        this.serviceCenterAddress = properties.getProperty(SERVICE_CENTER_ADDRESS);
        String strserviceCenterPort = properties.getProperty(SERVICE_CENTER_PORT);
        if(strserviceCenterPort != null && strserviceCenterPort != "")
        	this.serviceCenterPort = Integer.parseInt(strserviceCenterPort);
        else
        	this.serviceCenterPort = 0;
        for(int i = 1; i < 1000; i++)
        {
        	String serviceName = properties.getProperty(SERVICE_START_STRING + i + ".name");
        	String clientName = properties.getProperty(CLIENT_START_STRING + i + ".name");
        	if(serviceName != null && serviceName != ""){
        		ServicePropertyEntity entity = new ServicePropertyEntity();
            	entity.setServiceName(serviceName);
            	entity.setServiceGroup(properties.getProperty(SERVICE_START_STRING + i + ".group"));
            	entity.setServiceInterface(properties.getProperty(SERVICE_START_STRING + i + ".interface"));
            	entity.setServiceTarget(properties.getProperty(SERVICE_START_STRING + i + ".target"));
            	entity.setServiceVersion(properties.getProperty(SERVICE_START_STRING + i + ".version"));
            	serviceList.add(entity);
        	}
        	if(clientName != null && clientName != ""){
        		ClientPropertyEntity entity = new ClientPropertyEntity();
            	entity.setServiceName(clientName);
            	entity.setServiceGroup(properties.getProperty(CLIENT_START_STRING + i + ".group"));
            	entity.setServiceMethod(properties.getProperty(CLIENT_START_STRING + i + ".method"));
            	entity.setServiceVersion(properties.getProperty(CLIENT_START_STRING + i + ".version"));
            	entity.setId(properties.getProperty(CLIENT_START_STRING + i + ".id"));
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



	public List<ServicePropertyEntity> getServiceList() {
		return serviceList;
	}



	public List<ClientPropertyEntity> getServiceClientList() {
		return serviceClientList;
	}



	public String getServiceCenterAddress() {
		return serviceCenterAddress;
	}



	public int getServiceCenterPort() {
		return serviceCenterPort;
	}

	
}
