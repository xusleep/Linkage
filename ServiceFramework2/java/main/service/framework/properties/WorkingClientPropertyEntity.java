package service.framework.properties;

import static service.framework.common.ShareingData.CLIENT_START_STRING;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * this class used to read the properties from the client
 * @author zhonxu
 *
 */
public class WorkingClientPropertyEntity {
	private final List<ClientPropertyEntity> serviceClientList = new LinkedList<ClientPropertyEntity>();
	
	public WorkingClientPropertyEntity(String propertyFileName) throws IOException {
		InputStream inputStream = WorkingClientPropertyEntity.class.getClassLoader().getResourceAsStream(propertyFileName);
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
				ClientPropertyEntity entity = new ClientPropertyEntity();
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

	public List<ClientPropertyEntity> getServiceClientList() {
		return serviceClientList;
	}
}
