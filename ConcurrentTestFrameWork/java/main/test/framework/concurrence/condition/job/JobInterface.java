package test.framework.concurrence.condition.job;

public interface JobInterface {
	/**
	 * 此方法为运行并发方法前的准备
	 */
	void doBeforeJob();
	/**
	 * 此方法将在并发线程下运行，即由多个线程同时运行
	 */
	void doConcurrentJob();
	/**
	 * 此方法为运行完并发方法后的处理工作，在测试的时候可以在此方法里面加入测试比较
	 */
	void doAfterJob();
	
	/**
	 * 此方法将返回，该job应当启动的线程数目
	 * @return
	 */
	int  getThreadCount();
	
	/**
	 * 设置应当启动的线程的数目
	 */
	void setThreadCount(int count);
}
