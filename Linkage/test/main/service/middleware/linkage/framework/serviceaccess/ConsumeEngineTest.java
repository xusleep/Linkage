package service.middleware.linkage.framework.serviceaccess;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import service.middleware.linkage.framework.bootstrap.NIOMessageModeClientBootStrap;
import service.middleware.linkage.framework.bootstrap.NIOMessageModeServerBootStrap;
import service.middleware.linkage.framework.serviceaccess.entity.RequestEntity;
import service.middleware.linkage.framework.serviceaccess.entity.RequestResultEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;
import service.middleware.linkage.framework.utils.StringUtils;
import test.framework.concurrence.condition.MainConcurrentThread;
import test.framework.concurrence.condition.job.AbstractJob;
import test.framework.concurrence.condition.job.JobInterface;

public class ConsumeEngineTest {
	private static NIOMessageModeServerBootStrap serviceBootStrap;
	private static NIOMessageModeClientBootStrap clientBootStrap;
	private static Logger  logger = Logger.getLogger(ConsumeEngineTest.class); 
	
	@BeforeClass
	public static  void setUp(){
		try {
			serviceBootStrap = new NIOMessageModeServerBootStrap("service/middleware/linkage/framework/serviceaccess/conf/service_server.properties", 5);
			serviceBootStrap.run();
			clientBootStrap = new NIOMessageModeClientBootStrap("service/middleware/linkage/framework/serviceaccess/conf/client_client.properties", 1);
			clientBootStrap.run();
		} catch (Exception e) {
			assertTrue("There is exception when setup the test, exception detail: " + StringUtils.ExceptionStackTraceToString(e), false);
		}
	 }
	 
	 @Test
	 public void testBasicProcessRequest(){
		for(int i = 0; i < 1000; i++)
		{
			List<String> args = new LinkedList<String>();
			args.add("121");
			args.add("234");
			RequestEntity objRequestEntity = clientBootStrap.getServiceAccess().getServiceAccessEngine().createRequestEntity("calculator", args);
	        RequestResultEntity result = new RequestResultEntity();
	        result.setRequestID(objRequestEntity.getRequestID());
	        ServiceInformationEntity serviceInformationEntity = new ServiceInformationEntity();
	        serviceInformationEntity.setAddress(serviceBootStrap.getServicePropertyEntity().getServiceAddress());
	        serviceInformationEntity.setPort(serviceBootStrap.getServicePropertyEntity().getServicePort());
	        result = clientBootStrap.getServiceAccess().getServiceAccessEngine().basicProcessRequest(objRequestEntity, result, serviceInformationEntity, false);
	        assertTrue("Exception happen when request , exception: " + (result.isException() ? result.getException().getMessage() : ""), !result.isException());
	        assertTrue("result not right, 121 + 234 = 355, the result is " + result.getResponseEntity().getResult(), 
	        		result.getResponseEntity().getResult().equals("355"));
		}
	 }
	 
	 @Test
	 public void testConcurrentBasicProcessRequest(){
		// Test with not cached channel
		ConcurrentRequestJob job = new ConcurrentRequestJob(false);
		job.setThreadCount(1000);
		List<JobInterface> jobList = new LinkedList<JobInterface>();
		jobList.add(job);
		MainConcurrentThread mct1 = new MainConcurrentThread(jobList, false);
		mct1.start();
		try {
			mct1.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Test with cached channel
		ConcurrentRequestJob job1 = new ConcurrentRequestJob(true);
		job1.setThreadCount(1000);
		List<JobInterface> jobList1 = new LinkedList<JobInterface>();
		jobList1.add(job1);
		MainConcurrentThread mct2 = new MainConcurrentThread(jobList1, false);
		mct2.start();
		try {
			mct2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	 
	 
	 @AfterClass
	 public  static void clear(){
		 serviceBootStrap.shutdownImediate();
		 clientBootStrap.shutdownImediate();
	 }
	 
	/**
	 *  this is a job, which will be run by the concurrent test framework
	 * @author Smile
	 *
	 */
	private class ConcurrentRequestJob extends AbstractJob {
		
		private boolean isCachingChannel;

		private ConcurrentRequestJob(boolean isCachingChannel) {
			this.isCachingChannel = isCachingChannel;
		}

		@Override
		public void doBeforeJob() {

		}

		@Override
		public void doConcurrentJob() {
			Random r = new Random();
			int value1 = r.nextInt(10000);
			int value2 = r.nextInt(10000);
			int resultValue = value1 + value2;
			// TODO Auto-generated method stub
			List<String> args = new LinkedList<String>();
			args.add("" + value1);
			args.add("" + value2);
			RequestEntity objRequestEntity = clientBootStrap.getServiceAccess().getServiceAccessEngine().createRequestEntity("calculator", args);
			RequestResultEntity result = new RequestResultEntity();
			result.setRequestID(objRequestEntity.getRequestID());
			ServiceInformationEntity serviceInformationEntity = new ServiceInformationEntity();
			serviceInformationEntity.setAddress(serviceBootStrap.getServicePropertyEntity().getServiceAddress());
			serviceInformationEntity.setPort(serviceBootStrap.getServicePropertyEntity().getServicePort());
			result = clientBootStrap.getServiceAccess().getServiceAccessEngine().basicProcessRequest(objRequestEntity, result,serviceInformationEntity, isCachingChannel);
			assertTrue("Exception happen when request , exception: " + (result.isException() ? result.getException().getMessage() : ""), !result.isException());
			assertTrue(String.format("result not right, %s + %s = %s, the result is %s", value1, value2, resultValue, result.getResponseEntity().getResult()), result.getResponseEntity().getResult().equals("" + resultValue));
		}

		@Override
		public void doAfterJob() {
		}

	}
}
