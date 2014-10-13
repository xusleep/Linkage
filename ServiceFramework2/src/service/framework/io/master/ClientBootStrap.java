package service.framework.io.master;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import service.framework.io.event.ServiceOnMessageWriteEvent;
import service.framework.io.fire.MasterHandler;
import service.framework.io.handlers.ClientReadWriteHandler;
import service.framework.io.handlers.Handler;
import service.framework.io.server.WorkerPool;
import service.framework.io.server.WorkingChannel;
import service.framework.monitor.MonitorThread;

public class ClientBootStrap {
	
	public void start() throws IOException {
        List<Handler> eventConsumerList = new LinkedList<Handler>();
        eventConsumerList.add(new ClientReadWriteHandler());
		MasterHandler objMasterHandler = new MasterHandler(1, eventConsumerList);
		//启动事件处理分发线程, 即将任务分发到线程池，由线程池完成任务
    	objMasterHandler.start();
		WorkerPool.getInstance().start();
		new MonitorThread().start();
	}
	
	 
	public static void main(String[] args) {
		try {
			new ClientBootStrap().start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
