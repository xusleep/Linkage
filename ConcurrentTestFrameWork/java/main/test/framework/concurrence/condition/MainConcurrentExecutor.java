package test.framework.concurrence.condition;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class MainConcurrentExecutor extends Thread {
	//所以任务完成的信号
	private CountDownLatch doneSignal;
	//同时启动所有任务的信号
	private CountDownLatch startSignal;
	private List<JobInterface> jobList;
	private final ExecutorService executor;
	
	public MainConcurrentExecutor(List<JobInterface> jobList)
	{
		this(jobList, Executors.newFixedThreadPool(1000));
	}
	
	public MainConcurrentExecutor(List<JobInterface> jobList, ExecutorService executor)
	{ 
		this.jobList = jobList;
		this.executor = executor;
		init();
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
				Runnable task = new Runnable(){
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
				this.executor.execute(task);
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
		//执行完后回收服务
		this.executor.shutdown();
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
		TestJob job1 = new TestJob(1);
		job1.setThreadCount(1);
		TestJob job2 = new TestJob(2);
		job2.setThreadCount(10);
		List<JobInterface> jobList = new LinkedList<JobInterface>();
		jobList.add(job1);
		jobList.add(job2);
		//MainConcurrentExecutor ct = new MainConcurrentExecutor(jobList, new ThreadPerTaskExecutor());
		MainConcurrentExecutor ct = new MainConcurrentExecutor(jobList);
		ct.start();
		//BlockingQueue<BigInteger> bq = new ArrayBlockingQueue<BigInteger>();
		
	}
}
