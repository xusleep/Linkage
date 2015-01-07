package test.run;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import service.middleware.linkage.center.bootstrap.NIOCenterClientBootStrap;
import service.middleware.linkage.center.serviceaccess.NIORouteServiceAccess;
import service.middleware.linkage.framework.common.entity.RequestResultEntity;
import service.middleware.linkage.framework.common.entity.ServiceInformationEntity;
import test.framework.concurrence.condition.MainConcurrentThread;
import test.framework.concurrence.condition.job.AbstractJob;
import test.framework.concurrence.condition.job.JobInterface;

/**
 * 
 * @author Smile
 *
 */
public class StartClient extends AbstractJob {
	public static final AtomicInteger aint = new AtomicInteger(2320);
	private final AtomicBoolean isFailed = new AtomicBoolean(false);
	public static final AtomicInteger successCount = new AtomicInteger(0);
	public static final AtomicInteger requestCount = new AtomicInteger(0);
	
	public StartClient() {
	}
	
	@Override
	public void doBeforeJob() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doConcurrentJob() {
    	ServiceInformationEntity centerServiceInformationEntity = new ServiceInformationEntity();
    	centerServiceInformationEntity.setAddress("localhost");
    	centerServiceInformationEntity.setPort(5002);
		NIOCenterClientBootStrap clientBootStrap = new NIOCenterClientBootStrap("conf/client_client.properties", 5, centerServiceInformationEntity);
		clientBootStrap.run();;
		NIORouteServiceAccess cb = clientBootStrap.getServiceAccess();
		for(long i = 0; i < 1000; i++)
		{
			//System.out.println("request count ..." + requestCount.incrementAndGet());
	    	List<String> args1 = new LinkedList<String>();
	    	String a = "" + requestCount.incrementAndGet();
	    	String b = "" + aint.incrementAndGet();
	    	args1.add(a);
	    	args1.add(b);
	    	try
	    	{
				if(isFailed.get())
				{
					System.out.println("break ...");
					break;
				}
	    		RequestResultEntity result = cb.requestService("calculator", args1);
	    		if(result.isException())
	    		{
	    			result.getException().printStackTrace();
	    			System.out.println("exception happened " + result.getException().getMessage());
	    		}
	    		else
	    		{
	    			System.out.println("a = " + a + " + b = " + b + " = " + result.getResponseEntity().getResult());
	    			successCount.incrementAndGet();
	    		}
	    		
	    	}
	    	catch(Exception ex){
	    		ex.printStackTrace();
	    		isFailed.set(true);
	    	}
		}
	}

	@Override
	public void doAfterJob() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws IOException {
		StartClient job1 = new StartClient();
		job1.setThreadCount(400);
		List<JobInterface> jobList = new LinkedList<JobInterface>();
		jobList.add(job1);
		MainConcurrentThread mct1 = new MainConcurrentThread(jobList);
		mct1.start();
		System.out.println("成功的条数为:" + successCount.get());
	}
}
