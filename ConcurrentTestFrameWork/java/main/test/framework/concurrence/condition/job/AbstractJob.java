package test.framework.concurrence.condition.job;

public abstract class AbstractJob implements JobInterface {
	private int m_threadCount;

	@Override
	public final int getThreadCount() {
		// TODO Auto-generated method stub
		return m_threadCount;
	}

	@Override
	public final void setThreadCount(int count) {
		// TODO Auto-generated method stub
		this.m_threadCount = count;
	}

}
