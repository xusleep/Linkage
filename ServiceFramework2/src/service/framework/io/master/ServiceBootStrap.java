package service.framework.io.master;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.framework.io.event.ServiceEvent;
import service.framework.io.fire.MasterHandler;
import service.framework.io.handlers.Handler;
import service.framework.io.handlers.ReadWriteHandler;
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop(){
		
	}
	
	   public static void main(String[] args) {
	        try {
	            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("ServerServiceConfig.xml");
	            Server objServer = (Server)applicationContext.getBean("defaultServer");
	            List<Handler> eventConsumerList = new LinkedList<Handler>();
	            eventConsumerList.add(new ReadWriteHandler(applicationContext));
	    		//eventConsumerList.add(new ServiceRegisterHandler(applicationContext));
	    		MasterHandler objMasterHandler = new MasterHandler(1, eventConsumerList);
	            new ServiceBootStrap(objServer, objMasterHandler).start();
	        }
	        catch (Exception e) {
	            System.out.println("Server error: " + e.getMessage());
	            System.exit(-1);
	        }
	    }
}
