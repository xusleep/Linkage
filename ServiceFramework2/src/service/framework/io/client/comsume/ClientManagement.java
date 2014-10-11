package service.framework.io.client.comsume;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientManagement {
	public static ExecutorService objExecutorService = Executors.newFixedThreadPool(100);
	public void start(){
		objExecutorService = Executors.newFixedThreadPool(100);
	}
	
	public void stop(){
		objExecutorService.shutdown();
	}
}
