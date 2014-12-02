package test.framework.concurrence.condition;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import test.framework.concurrence.condition.job.JobInterface;
import test.framework.concurrence.condition.job.TestJob;

/**
 *  该类用于，同时启动多个job的多个线程，使其并发运行
 *  所有的job应当继承JobInterface方法
 *  由于主类也是一个线程，若要等待该线程的结束，请使用.join();方法
 *  
 * @author Smile
 *
 */
public class MainConcurrentThread extends Thread {
	//所以任务完成的信号
	private CountDownLatch doneSignal;
	//同时启动所有任务的信号
	private CountDownLatch startSignal;
	private List<JobInterface> jobList;
	private boolean deadMonitorCheck;
	
	public MainConcurrentThread(List<JobInterface> jobList, boolean deadMonitorCheck){
		this.jobList = jobList;
		init();
		this.deadMonitorCheck = deadMonitorCheck;
	}
	
	public MainConcurrentThread(List<JobInterface> jobList)
	{ 
		this(jobList, false);
	}
	
	/**
	 * 返回需要创建的所有的线程数目
	 * @return
	 */
	private int getAllJobThreadCount()
	{
		int countJobs = jobList.size();
		int countAllJobThreads = 0;
		for(int i = 0; i < countJobs; i++)
		{
			countAllJobThreads = countAllJobThreads + jobList.get(i).getThreadCount();
		}
		return countAllJobThreads;
	}
	
	/**
	 * 初始化
	 * 
	 */
	private void init()
	{
		int allJobThreadCount = getAllJobThreadCount();
		this.doneSignal = new CountDownLatch(allJobThreadCount);
		this.startSignal = new CountDownLatch(1);
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		init();
		ScheduledExecutorService executor = null;
		if(this.deadMonitorCheck){
			executor = Executors.newScheduledThreadPool(1);
			executor.scheduleAtFixedRate(new FindMonitorRunnable(), 0, 1, TimeUnit.SECONDS);
		}
		
		int countJobs = jobList.size();
		for(int i = 0; i < countJobs; i++)
		{
			JobInterface job = jobList.get(i);
			//运行job前，先运行一些准备任务
			job.doBeforeJob();
		}
		
		for(int i = 0; i < countJobs; i++)
		{
			final JobInterface job = jobList.get(i);
			int jobThreadCount = job.getThreadCount();
			//启动所有的线程
			for(int j = 0; j < jobThreadCount; j++)
			{
				Thread thr = new Thread(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
							//等待启动信号完成，在前面如果 countdown后，这里将继续执行下去
							awaitStartSignal();
							job.doConcurrentJob();
							//计数减一，证明已经有一个线程完成了job
							countDownDoneSignal();
						}
					};
				thr.start();
			}
		}
		
		//其计数只有1，countdown以后，所有启动的线程将继续运行，不再阻塞
		this.countDownStartSignal();
		
		//等待所有子线程完成任务
		this.awaitDoneSignal();
		for(int i = 0; i < countJobs; i++)
		{
			JobInterface job = jobList.get(i);
			//运行完job后，先运行一些后续任务
			job.doAfterJob();
		}
		if(this.deadMonitorCheck){
			executor.shutdownNow();
		}
	}

	/**
	 * 计数器减一，这里必须使用同步方法 主线程的count down 主要用于主线程 等待所有的任务完成
	 */
	public synchronized void countDownDoneSignal()
	{
		if(this.doneSignal != null)
		{
			this.doneSignal.countDown();
		}
	}
	/**
	 * 阻塞线程，这里必须使用同步方法 主线程的count down 主要用于主线程 等待所有的任务完成
	 */
	public void awaitDoneSignal()
	{
		if(this.doneSignal != null)
		{
			try {
				this.doneSignal.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * 计数器减一，这里必须使用同步方法 主线程的count down 主要用于并发线程 等待所有的任务完成
	 */
	public synchronized void countDownStartSignal()
	{
		if(this.startSignal != null)
		{
			this.startSignal.countDown();
		}
	}
	/**
	 * 阻塞线程，这里必须使用同步方法 并发线程的count down 主要用于并发线程 等待所有的任务完成
	 */
	public void awaitStartSignal()
	{
		if(this.startSignal != null)
		{
			try {
				this.startSignal.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
//		TestJob job1 = new TestJob(1);
//		job1.setThreadCount(1);
//		TestJob job2 = new TestJob(2);
//		job2.setThreadCount(10);
//		List<JobInterface> jobList = new LinkedList<JobInterface>();
//		jobList.add(job1);
//		jobList.add(job2);
//		MainConcurrentThread ct = new MainConcurrentThread(jobList);
//		ct.start();
//		AtomicInteger t = new AtomicInteger(10);
		
		Thread t1 = new Thread(){

			@Override
			public void run() {
					int sum = 1000;
					for(int j = 0; j < 10; j++)
					{
						if(Thread.interrupted())
						{
							break;
						}
						// TODO Auto-generated method stub
						for(int i = 0; i < 1000000000; i++)
						{
							for(int l = 0; l < 1000000000; l++)
							{
								sum += 2;
								
							}
						}
						System.out.println(sum);
					}
			}
			
			
		};
		t1.start();
		System.out.println("test 112");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t1.interrupt();
		System.out.println("test 112");
		if(t1.interrupted())
		{
			System.out.println("test");
		}
	}
}
