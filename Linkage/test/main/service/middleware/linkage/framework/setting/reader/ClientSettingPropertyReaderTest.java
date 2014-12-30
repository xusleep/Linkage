package service.middleware.linkage.framework.setting.reader;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import service.middleware.linkage.framework.setting.reader.ClientSettingPropertyReader;
import service.middleware.linkage.framework.setting.reader.ClientSettingReader;

public class ClientSettingPropertyReaderTest {
	
	ClientSettingReader workingClientPropertyEntity;
	
	@Before
	public void setUp(){
		try {
			workingClientPropertyEntity = new ClientSettingPropertyReader("service/framework/comsume/conf/client_client.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void normalTest(){
		assertTrue("the count is not right.", workingClientPropertyEntity.getServiceClientList().size() == 5);
		try {
			workingClientPropertyEntity = new ClientSettingPropertyReader("service/framework/comsume/conf/client_client.properties1");
		} catch (IOException e) {
		}
		assertTrue("the count is not right.", workingClientPropertyEntity.getServiceClientList().size() == 0);
	}
	
}
