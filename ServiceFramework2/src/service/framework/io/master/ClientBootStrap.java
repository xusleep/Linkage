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
		//�����¼�����ַ��߳�, ��������ַ����̳߳أ����̳߳��������
    	objMasterHandler.start();
		WorkerPool.getInstance().start();
		// ���һ��Socketͨ��  
        SocketChannel channel = SocketChannel.open();  
        // ����ͨ��Ϊ������  
        channel.configureBlocking(false);   
          
        // �ͻ������ӷ�����,��ʵ����ִ�в�û��ʵ�����ӣ���Ҫ��listen���������е�  
        //��channel.finishConnect();�����������  
        channel.connect(new InetSocketAddress(address, port));
        // ����������ӣ����������  
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
