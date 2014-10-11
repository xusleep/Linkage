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
        	//�������ݷ����߳�
			new Thread(objServer).start();
			WorkerPool.getInstance().start();
			//�����¼�����ַ��߳�, ��������ַ����̳߳أ����̳߳��������
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
