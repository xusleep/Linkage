package test.framework.concurrence.condition;

import test.framework.concurrence.condition.job.JobInterface;

public class ConcurrentThread extends Thread{
	private MainConcurrentThread mainThread;
	private JobInterface job;
	
	public ConcurrentThread(MainConcurrentThread mainThread, JobInterface job)
	{
		this.mainThread = mainThread;
		this.job = job;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		if(this.getMainThread() != null)
		{
			//等待启动信号完成，在前面如果 countdown后，这里将继续执行下去
			this.getMainThread().awaitStartSignal();
			if(this.job != null)
			{
				this.job.doConcurrentJob();
			}
			//计数减一，证明已经有一个线程完成了job
			this.getMainThread().countDownDoneSignal();
		}
	}
	
	public MainConcurrentThread getMainThread() {
		return mainThread;
	}

	public void setMainThread(MainConcurrentThread mainThread) {
		this.mainThread = mainThread;
	}
}
