package service.framework.io.master;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.ApplicationContext;

import service.framework.io.event.ServiceEvent;
import service.framework.io.fire.MasterHandler;
import service.framework.io.handlers.Handler;
import service.framework.io.server.Server;
import service.framework.io.server.WorkerPool;
import service.framework.monitor.MonitorThread;

public class ServiceBootStrap {
	private final Server objServer;
	private final MasterHandler objMasterHandler;

	public ServiceBootStrap(Server objServer, MasterHandler objMasterHandler) throws Exception{
		this.objServer = objServer;
		this.objMasterHandler = objMasterHandler;
	}
	
	public void start(){
        try {
        	//启动数据服务线程
			new Thread(objServer).start();
			WorkerPool.getInstance().start();
			//启动事件处理分发线程, 即将任务分发到线程池，由线程池完成任务
        	objMasterHandler.start();
        	MonitorThread mt = new MonitorThread();
        	mt.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop(){
		
	}
}
