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
    //通道管理器  
    private Selector selector;  
    
    
	public ClientTask(String address, int port, RequestEntity objRequestData) throws IOException
	{
		this.objRequestEntity = objRequestData;
		
	}
	
	@Override
	public Object call() {
        
        return objRequestEntity.getRequestID();
	}
	

}
