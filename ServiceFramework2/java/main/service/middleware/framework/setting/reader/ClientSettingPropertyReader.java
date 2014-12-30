package service.middleware.framework.setting.reader;

import static service.middleware.framework.common.ShareingData.CLIENT_START_STRING;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import service.middleware.framework.setting.ClientSettingEntity;

public class ClientSettingPropertyReader implements ClientSettingReader {
	private final List<ClientSettingEntity> serviceClientList = new LinkedList<ClientSettingEntity>();
	
	public ClientSettingPropertyReader(String propertyFileName) throws IOException {
		InputStream inputStream = ClientSettingReader.class.getClassLoader().getResourceAsStream(propertyFileName);
		//create a new Properties
        Properties properties = new Properties(); 
        // load properties 
        properties.load(inputStream);
        if(properties != null)
        {
        	readClientList(properties);
        }
	}
	
	/**
	 * read the information from the properties 
	 * @param properties
	 */
	private void readClientList(Properties properties){
		for (int i = 1; i < 1000; i++) {
			String clientName = properties.getProperty(CLIENT_START_STRING + i
					+ ".name");
			if (clientName != null && clientName != "") {
				ClientSettingEntity entity = new ClientSettingEntity();
				entity.setServiceName(clientName);
				entity.setServiceGroup(properties
						.getProperty(CLIENT_START_STRING + i + ".group"));
				entity.setServiceMethod(properties
						.getProperty(CLIENT_START_STRING + i + ".method"));
				entity.setServiceVersion(properties
						.getProperty(CLIENT_START_STRING + i + ".version"));
				entity.setId(properties.getProperty(CLIENT_START_STRING + i
						+ ".id"));
				serviceClientList.add(entity);
			}
			if (clientName == null || clientName == "") {
				break;
			}
		}
	}
	
	public List<ClientSettingEntity> getServiceClientList() {
		return serviceClientList;
	}
}
