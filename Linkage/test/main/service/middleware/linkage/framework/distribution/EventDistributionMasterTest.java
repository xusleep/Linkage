package service.middleware.linkage.framework.distribution;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import service.middleware.linkage.framework.event.ServiceEvent;
import service.middleware.linkage.framework.handlers.NIOMessageEventDistributionMaster;
import service.middleware.linkage.framework.handlers.Handler;
import test.framework.concurrence.condition.MainConcurrentThread;
import test.framework.concurrence.condition.job.AbstractJob;
import test.framework.concurrence.condition.job.JobInterface;

/**
 * this is used to test the EventDistribution{
 * @author Smil
 *
 */
public class EventDistributionMasterTest {
	
	private static NIOMessageEventDistributionMaster eventDistributionMaster;
	private static Logger  logger = Logger.getLogger(EventDistributionMasterTest.class); 
	
	@Before
	public void setUp(){
		eventDistributionMaster = new NIOMessageEventDistributionMaster(100);
		eventDistributionMaster.start();
	}
	
	@Test
	public void testSubmitServiceEvent(){
		eventDistributionMaster.addHandler(new TestMultiHandler());
		Random r = new Random();
		int value1 = r.nextInt(1000);
		int value2 = r.nextInt(1000);
		int result = value1 * value2;
		TestServiceEvent objTestServiceEvent = new TestServiceEvent(value1, value2, result);
		eventDistributionMaster.submitServiceEvent(objTestServiceEvent);
		logger.debug("result value is " + objTestServiceEvent.getResult()) ;
		assertTrue(String.format("the value is not equals to expected value, %s * %s = %s", value1, value2, result), 
				objTestServiceEvent.getResult() == objTestServiceEvent.getExpected());
	}
	
	@Test
	public void testConcurrentSubmitServiceEvent(){
		eventDistributionMaster.addHandler(new TestMultiHandler());
		ConcurrentSubmitEventJob job = new ConcurrentSubmitEventJob();
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
	}
	
	
	/**
	 * this test is to test concurrent submit an event, 
	 * it's a high pressure test
	 */
	@Test
	public void testHighPressSubmitServiceEvent(){
		eventDistributionMaster.addHandler(new TestMultiHandler());
		logger.debug("testHighPressSubmitServiceEvent start.");
		ConcurrentPressureSubmitEventJob job = new ConcurrentPressureSubmitEventJob();
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
		logger.debug("testHighPressSubmitServiceEvent end.");
	}
	
	@Test
	public void testMaxSubmitServiceEvent(){
		// Don't register a hand
		//eventDistributionMaster.registerHandler(new TestMultiHandler());
		eventDistributionMaster.addHandler(new TestMultiHandlerLongTime());
		for(int i = 0; i < 10000000; i++){
			Random r = new Random();
			int value1 = r.nextInt(1000);
			int value2 = r.nextInt(1000);
			int result = value1 * value2;
			TestServiceEvent objTestServiceEvent = new TestServiceEvent(value1, value2, result);
			eventDistributionMaster.submitServiceEvent(objTestServiceEvent);
		}
	}
	
	
	
	
	@After
	public void clear(){
		eventDistributionMaster.shutdown();
	}
	
	/**
	 *  this is a job, which will be run by the concurrent test framework
	 * @author Smile
	 *
	 */
	private class ConcurrentPressureSubmitEventJob extends AbstractJob {

		@Override
		public void doBeforeJob() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void doConcurrentJob() {
			// TODO Auto-generated method stub
			for(int i = 0; i < 100; i++)
			{
				Random r = new Random();
				int value1 = r.nextInt(1000);
				int value2 = r.nextInt(1000);
				int result = value1 * value2;
				TestServiceEvent objTestServiceEvent = new TestServiceEvent(value1, value2, result);
				eventDistributionMaster.submitServiceEvent(objTestServiceEvent);
				assertTrue(String.format("the value is not equals to expected value, %s * %s = %s", value1, value2, result), 
						objTestServiceEvent.getResult() == objTestServiceEvent.getExpected());
			}
		}

		@Override
		public void doAfterJob() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 *  this is a job, which will be run by the concurrent test framework
	 * @author Smile
	 *
	 */
	private class ConcurrentSubmitEventJob extends AbstractJob {

		@Override
		public void doBeforeJob() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void doConcurrentJob() {
			// TODO Auto-generated method stub
			Random r = new Random();
			int value1 = r.nextInt(1000);
			int value2 = r.nextInt(1000);
			int result = value1 * value2;
			TestServiceEvent objTestServiceEvent = new TestServiceEvent(value1, value2, result);
			eventDistributionMaster.submitServiceEvent(objTestServiceEvent);
			assertTrue(String.format("the value is not equals to expected value, %s * %s = %s", value1, value2, result), 
					objTestServiceEvent.getResult() == objTestServiceEvent.getExpected());
		}

		@Override
		public void doAfterJob() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 *  this handler is used to multiply the two values in the test service event
	 * @author Smile
	 *
	 */
	private static class TestMultiHandler extends Handler{

		@Override
		public void handleRequest(ServiceEvent event) throws IOException,
				Exception {
			TestServiceEvent objTestServiceEvent = (TestServiceEvent)event;
			objTestServiceEvent.setResult(objTestServiceEvent.getA() * objTestServiceEvent.getB());
		}
	}
	
	/**
	 *  this handler is used to multiply the two values in the test service event, 
	 *  but this handler will take a very long time 10s
	 * @author Smile
	 *
	 */
	private static class TestMultiHandlerLongTime extends Handler{

		@Override
		public void handleRequest(ServiceEvent event) throws IOException,
				Exception {
			TestServiceEvent objTestServiceEvent = (TestServiceEvent)event;
			objTestServiceEvent.setResult(objTestServiceEvent.getA() * objTestServiceEvent.getB());
			Thread.sleep(100000);
		}
	}
	
	/**
	 * this event is used for this test only
	 * @author Smile
	 *
	 */
	private class TestServiceEvent implements ServiceEvent{
		private int a;
		private int b;
		private int expected;
		private int result;
		private CountDownLatch resultSignal = new CountDownLatch(1);
		
		public TestServiceEvent(int a, int b, int expected){
			this.a = a;
			this.b = b;
			this.expected = expected;
		}

		public int getA() {
			return a;
		}

		public int getB() {
			return b;
		}

		public int getResult() {
			try {
				resultSignal.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}

		public void setResult(int result) {
			this.result = result;
			resultSignal.countDown();
		}

		public int getExpected() {
			return expected;
		}	
	}
}
