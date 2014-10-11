package service.framework.run;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.framework.io.client.comsume.ClientManagement;
import service.framework.io.client.comsume.ConsumerBean;
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
	
	public StartClient(ConsumerBean cb) {
		this.cb = cb;
	}
	
	@Override
	public void doBeforeJob() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doConcurrentJob() {
		try {
			for(long i = 1; i < 100000000; i++)
			{
				
		    	List<String> args1 = new LinkedList<String>();
		    	String a = "" + aint.incrementAndGet();
		    	String b = "" + aint.incrementAndGet();
		    	args1.add(a);
		    	args1.add(b);
		    	try
		    	{
		    		long id = cb.prcessRequest(args1);
		    		System.out.println("Thread.currentThread().getId() : " + Thread.currentThread().getId() + "  a = " + a + " b = "  + b + " result : " + cb.getResult(id));
		    	}
		    	catch(Exception ex){
		    		long id = cb.prcessRequest(args1);
		    		System.out.println("retry Thread.currentThread().getId() : " + Thread.currentThread().getId() + "  a = " + a + " b = "  + b + " result : " + cb.getResult(id));
		    	
		    	}
			}
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			aint.decrementAndGet();
			aint.decrementAndGet();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			aint.decrementAndGet();
			aint.decrementAndGet();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			aint.decrementAndGet();
			aint.decrementAndGet();
		}
		catch(Exception ex){
			ex.printStackTrace();
			aint.decrementAndGet();
			aint.decrementAndGet();
		}
	}

	@Override
	public void doAfterJob() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
	 	ApplicationContext applicationContext = new ClassPathXmlApplicationContext("ClientServiceConfig.xml");
    	ClientManagement cmm = new ClientManagement();
		StartClient job1 = new StartClient((ConsumerBean)applicationContext.getBean("addService"));
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
		System.out.println("成功的条数为:" + (StartClient.aint.get() - 2320) / 2);
	}
}
