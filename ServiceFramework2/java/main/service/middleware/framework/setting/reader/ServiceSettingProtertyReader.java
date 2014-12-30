package service.middleware.framework.setting.reader;

import static service.middleware.framework.common.ShareingData.SERVICE_ADDRESS;
import static service.middleware.framework.common.ShareingData.SERVICE_PORT;
import static service.middleware.framework.common.ShareingData.SERVICE_START_STRING;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import service.middleware.framework.setting.ServiceSettingEntity;

public class ServiceSettingProtertyReader implements ServiceSettingReader {
	private final String serviceAddress;
	private final int servicePort;
	private final List<ServiceSettingEntity> serviceList = new LinkedList<ServiceSettingEntity>();

	public ServiceSettingProtertyReader(String propertyFileName){
		InputStream inputStream = ServiceSettingReader.class.getClassLoader().getResourceAsStream(propertyFileName);
		//创建一个Properties容器 
        Properties properties = new Properties(); 
        //从流中加载properties文件信息 
        try {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} 
        this.serviceAddress = properties.getProperty(SERVICE_ADDRESS);
        String strServicePort = properties.getProperty(SERVICE_PORT);
        if(strServicePort != null && strServicePort != "")
        	this.servicePort = Integer.parseInt(strServicePort);
        else
        	this.servicePort = 0;
        for(int i = 1; i < 1000; i++)
        {
        	String serviceName = properties.getProperty(SERVICE_START_STRING + i + ".name");
        	if(serviceName != null && serviceName != ""){
        		ServiceSettingEntity entity = new ServiceSettingEntity();
            	entity.setServiceName(serviceName);
            	entity.setServiceGroup(properties.getProperty(SERVICE_START_STRING + i + ".group"));
            	entity.setServiceInterface(properties.getProperty(SERVICE_START_STRING + i + ".interface"));
            	entity.setServiceTarget(properties.getProperty(SERVICE_START_STRING + i + ".target"));
            	entity.setServiceVersion(properties.getProperty(SERVICE_START_STRING + i + ".version"));
            	serviceList.add(entity);
        	}
        	if(serviceName == null || serviceName == ""){
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

	public List<ServiceSettingEntity> getServiceList() {
		return serviceList;
	}
}
