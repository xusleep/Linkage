package service.framework.run;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import service.framework.io.client.comsume.ClientManagement;
import service.framework.io.client.comsume.ConsumerBean;
import service.framework.io.client.comsume.RequestResultEntity;
import service.framework.io.master.ClientBootStrap;
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
	private final ConsumerBean cb;
	private final AtomicBoolean isFailed = new AtomicBoolean(false);
	public static final AtomicInteger successCount = new AtomicInteger(0);
	public static final AtomicInteger requestCount = new AtomicInteger(0);
	
	public StartClient(ConsumerBean cb) {
		this.cb = cb;
	}
	
	@Override
	public void doBeforeJob() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doConcurrentJob() {
		for(long i = 0; i < 10000000; i++)
		{
			System.out.println("request count ..." + requestCount.incrementAndGet());
	    	List<String> args1 = new LinkedList<String>();
	    	String a = "" + requestCount.get();
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
		ClientBootStrap.getInstance().start();
    	ClientManagement cmm = new ClientManagement();
		StartClient job1 = new StartClient(ClientBootStrap.getInstance().getConsumerBean());
		job1.setThreadCount(10);
		List<JobInterface> jobList = new LinkedList<JobInterface>();
		jobList.add(job1);
		MainConcurrentThread mct1 = new MainConcurrentThread(jobList);
		mct1.start();
		try {
			mct1.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cmm.stop();
		System.out.println("成功的条数为:" + successCount.get());
	}
}
