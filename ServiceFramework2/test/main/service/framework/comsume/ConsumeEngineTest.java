package service.framework.comsume;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import service.framework.common.entity.RequestEntity;
import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ServiceInformationEntity;
import service.framework.distribution.EventDistributionMaster;
import service.framework.io.client.DefaultClient;
import service.framework.io.common.DefaultWorkerPool;
import service.framework.io.common.WorkerPool;
import service.framework.properties.WorkingClientPropertyEntity;

public class ConsumeEngineTest {
	
	private ConsumeEngine consumeEngine;
	
	@Before
	@SuppressWarnings("unchecked")
	 public  void setUp(){
		EventDistributionMaster eventDistributionMaster = new EventDistributionMaster(10);
		WorkerPool objWorkerPool = new DefaultWorkerPool(eventDistributionMaster);
		WorkingClientPropertyEntity objServicePropertyEntity = new WorkingClientPropertyEntity("service/framework/comsume/conf/client_client.properties");
		DefaultClient client = new DefaultClient(eventDistributionMaster, objWorkerPool);
		new Thread(client).start();
		consumeEngine = new ConsumeEngine(objServicePropertyEntity, objWorkerPool);
	 }
	 
	 @Test
	 public void testBasicProcessRequest(){
		List<String> args = new LinkedList<String>();
		args.add("121");
		args.add("234");
		RequestEntity objRequestEntity = consumeEngine.createRequestEntity("calculator", args);
        RequestResultEntity result = new RequestResultEntity();
        result.setRequestID(objRequestEntity.getRequestID());
        ServiceInformationEntity serviceInformationEntity = new ServiceInformationEntity();
        serviceInformationEntity.setAddress("localhost");
        serviceInformationEntity.setPort(5003);
        result = consumeEngine.basicProcessRequest(objRequestEntity, result, serviceInformationEntity, false);
        assertTrue("Exception happen when request , exception: " + (result.isException() ? "" : result.getException().getMessage()), !result.isException());
        assertTrue("result not right, 121 + 234 = 355, the result is " + result.getResponseEntity().getResult(), 
        		result.getResponseEntity().getResult().equals("3515"));
	 }
}
