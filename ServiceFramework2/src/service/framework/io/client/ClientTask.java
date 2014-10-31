package service.framework.io.client;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import service.framework.common.entity.RequestEntity;

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
