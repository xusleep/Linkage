package service.framework.properties;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class WorkingClientPropertyEntityTest {
	
	WorkingClientPropertyEntity workingClientPropertyEntity;
	
	@Before
	public void setUp(){
		try {
			workingClientPropertyEntity = new WorkingClientPropertyEntity("service/framework/comsume/conf/client_client.properties");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void normalTest(){
		assertTrue("the count is not right.", workingClientPropertyEntity.getServiceClientList().size() == 5);
		try {
			workingClientPropertyEntity = new WorkingClientPropertyEntity("service/framework/comsume/conf/client_client.properties1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		assertTrue("the count is not right.", workingClientPropertyEntity.getServiceClientList().size() == 0);
	}
	
}
