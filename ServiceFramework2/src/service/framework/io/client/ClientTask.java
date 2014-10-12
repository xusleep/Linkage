package service.framework.io.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import service.framework.io.context.DefaultServiceContext;
import service.framework.io.event.ServiceOnMessageWriteEvent;
import service.framework.io.server.WorkerPool;
import service.framework.io.server.WorkingChannel;
import service.framework.protocol.ShareingProtocolData;
import service.framework.provide.entity.RequestEntity;
import service.framework.provide.entity.ResponseEntity;
import service.framework.serialization.SerializeUtils;

public class ClientTask implements Callable {
    private final RequestEntity objRequestEntity;
	public static AtomicLong idCounter = new AtomicLong(0);
    //ͨ��������  
    private Selector selector;  
    private WorkingChannel newWorkingChannel;
    
	public ClientTask(String address, int port, RequestEntity objRequestData) throws IOException
	{
		this.objRequestEntity = objRequestData;
		this.newWorkingChannel = newWorkingChannel(address, port);
	}
	
	@Override
	public Object call() {
        ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(newWorkingChannel);
        String sendData = SerializeUtils.serializeRequest(this.objRequestEntity);
        byte[] data;
		try {
			data = sendData.getBytes(ShareingProtocolData.FRAMEWORK_IO_ENCODING);
			objServiceOnMessageWriteEvent.setMessage(data);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        newWorkingChannel.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
        newWorkingChannel.getWorker().writeFromUser(newWorkingChannel);
        return objRequestEntity.getRequestID();
	}
	
	public WorkingChannel newWorkingChannel(String address, int port) throws IOException{
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
        return objWorkingChannel;
	}
}
