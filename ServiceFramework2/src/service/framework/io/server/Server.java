package service.framework.io.server;

public interface Server extends Runnable{
	public WorkerPool getWorkPool();
}
