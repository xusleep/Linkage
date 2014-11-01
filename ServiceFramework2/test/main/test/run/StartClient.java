package test.run;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import service.framework.bootstrap.ClientBootStrap;
import service.framework.common.entity.RequestResultEntity;
import service.framework.comsume.ConsumerBean;
import zhonglin.test.framework.concurrence.condition.MainConcurrentThread;
import zhonglin.test.framework.concurrence.condition.job.AbstractJob;
import zhonglin.test.framework.concurrence.condition.job.JobInterface;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
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
		ClientBootStrap clientBootStrap = new ClientBootStrap("conf/client.properties", 5);
		clientBootStrap.run();;
		ConsumerBean cb = clientBootStrap.getConsumerBean();
		for(long i = 0; i < 1; i++)
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
	    		RequestResultEntity result = cb.prcessRequest("calculator", args1);
	    		System.out.println("a = " + a + " + b = " + b + " = " + result.getResponseEntity().getResult());
	    		successCount.incrementAndGet();
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
		job1.setThreadCount(1);
		List<JobInterface> jobList = new LinkedList<JobInterface>();
		jobList.add(job1);
		MainConcurrentThread mct1 = new MainConcurrentThread(jobList);
		mct1.start();
		System.out.println("成功的条数为:" + successCount.get());
	}
}
