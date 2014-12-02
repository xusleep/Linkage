package service.framework.comsume;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import service.framework.bootstrap.ClientBootStrap;
import service.framework.bootstrap.ServerBootStrap;
import service.framework.common.StringUtils;
import service.framework.common.entity.RequestEntity;
import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ServiceInformationEntity;

public class ConsumeEngineTest {
	private ServerBootStrap serviceBootStrap;
	private ClientBootStrap clientBootStrap;
	private static Logger  logger = Logger.getLogger(ConsumeEngineTest.class); 
	
	@Before
	@SuppressWarnings("unchecked")
	public  void setUp(){
		try {
			serviceBootStrap = new ServerBootStrap("service/framework/comsume/conf/service_server.properties", 5);
			serviceBootStrap.run();
			clientBootStrap = new ClientBootStrap("service/framework/comsume/conf/client_client.properties", 1);
			clientBootStrap.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			assertTrue("There is exception when setup the test, exception detail: " + StringUtils.ExceptionStackTraceToString(e), false);
		}
	 }
	 
	 @Test
	 public void testBasicProcessRequest(){
		logger.debug("Start 1000 times test");
		for(int i = 0; i < 1000; i++)
		{
			logger.debug("Start " + i + " time test");
			List<String> args = new LinkedList<String>();
			args.add("121");
			args.add("234");
			RequestEntity objRequestEntity = clientBootStrap.getConsume().getConsumeEngine().createRequestEntity("calculator", args);
	        RequestResultEntity result = new RequestResultEntity();
	        result.setRequestID(objRequestEntity.getRequestID());
	        ServiceInformationEntity serviceInformationEntity = new ServiceInformationEntity();
	        serviceInformationEntity.setAddress(serviceBootStrap.getServicePropertyEntity().getServiceAddress());
	        serviceInformationEntity.setPort(serviceBootStrap.getServicePropertyEntity().getServicePort());
	        result = clientBootStrap.getConsume().getConsumeEngine().basicProcessRequest(objRequestEntity, result, serviceInformationEntity, false);
	        assertTrue("Exception happen when request , exception: " + (result.isException() ? result.getException().getMessage() : ""), !result.isException());
	        assertTrue("result not right, 121 + 234 = 355, the result is " + result.getResponseEntity().getResult(), 
	        		result.getResponseEntity().getResult().equals("355"));
		}
	 }
	 
	 
	 @After
	 @SuppressWarnings("unchecked")
	 public  void clear(){
		 serviceBootStrap.shutdownImediate();
		 clientBootStrap.shutdownImediate();
	 }
}
