package service.framework.properties;

import static service.framework.common.ShareingData.CLIENT_START_STRING;
import static service.framework.common.ShareingData.SERVICE_CENTER_ADDRESS;
import static service.framework.common.ShareingData.SERVICE_CENTER_PORT;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class WorkingClientPropertyEntity {
	private final String serviceCenterAddress;
	private final int serviceCenterPort;
	private final List<ClientPropertyEntity> serviceClientList = new LinkedList<ClientPropertyEntity>();
	
	public WorkingClientPropertyEntity(String propertyFileName){
		InputStream inputStream = WorkingClientPropertyEntity.class.getClassLoader().getResourceAsStream(propertyFileName);
		//创建一个Properties容器 
        Properties properties = new Properties(); 
        //从流中加载properties文件信息 
        try {
			properties.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        this.serviceCenterAddress = properties.getProperty(SERVICE_CENTER_ADDRESS);
        String strserviceCenterPort = properties.getProperty(SERVICE_CENTER_PORT);
        if(strserviceCenterPort != null && strserviceCenterPort != "")
        	this.serviceCenterPort = Integer.parseInt(strserviceCenterPort);
        else
        	this.serviceCenterPort = 0;
        for(int i = 1; i < 1000; i++)
        {
        	String clientName = properties.getProperty(CLIENT_START_STRING + i + ".name");
        	if(clientName != null && clientName != ""){
        		ClientPropertyEntity entity = new ClientPropertyEntity();
            	entity.setServiceName(clientName);
            	entity.setServiceGroup(properties.getProperty(CLIENT_START_STRING + i + ".group"));
            	entity.setServiceMethod(properties.getProperty(CLIENT_START_STRING + i + ".method"));
            	entity.setServiceVersion(properties.getProperty(CLIENT_START_STRING + i + ".version"));
            	entity.setId(properties.getProperty(CLIENT_START_STRING + i + ".id"));
            	serviceClientList.add(entity);
        	}
        	if(clientName == null || clientName == ""){
        		break;
        	}
        }
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
