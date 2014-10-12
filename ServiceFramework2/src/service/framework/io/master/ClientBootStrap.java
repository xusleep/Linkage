package service.framework.io.master;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import service.framework.io.event.ServiceOnMessageWriteEvent;
import service.framework.io.fire.MasterHandler;
import service.framework.io.handlers.Handler;
import service.framework.io.server.WorkerPool;
import service.framework.io.server.WorkingChannel;

public class ClientBootStrap {
	
	public void start(String address, int port) throws IOException{

        List<Handler> eventConsumerList = new LinkedList<Handler>();
		MasterHandler objMasterHandler = new MasterHandler(1, eventConsumerList);
		//启动事件处理分发线程, 即将任务分发到线程池，由线程池完成任务
    	objMasterHandler.start();
		WorkerPool.getInstance().start();
		// 获得一个Socket通道  
        SocketChannel channel = SocketChannel.open();  
        // 设置通道为非阻塞  
        channel.configureBlocking(false);   
          
        // 客户端连接服务器,其实方法执行并没有实现连接，需要在listen（）方法中调  
        //用channel.finishConnect();才能完成连接  
        channel.connect(new InetSocketAddress(address, port));
        // 如果正在连接，则完成连接  
        if(channel.isConnectionPending()){  
            channel.finishConnect();  
        } 
        
        WorkingChannel objWorkingChannel = WorkerPool.getInstance().register(channel);
        ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(objWorkingChannel);
        objServiceOnMessageWriteEvent.setMessage("haha".getBytes());
        objWorkingChannel.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
        objWorkingChannel.getWorker().writeFromUser(objWorkingChannel);
	}
	
	 
	public static void main(String[] args) {
		try {
			new ClientBootStrap().start("localhost", 5001);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
