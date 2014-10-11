package service.framework.io.handlers;

import static service.framework.io.fire.Fires.fireRegisterChannel;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import service.framework.io.context.ServiceContext;
import service.framework.io.event.ServiceEvent;
import service.framework.io.event.ServiceOnAcceptedEvent;

/**
 * 此类，主要用于处理接收客户端连接的事件，连接后马上设置通道可读，
 * 以便于后面的处理
 * @author zhonxu
 *
 */
public class AcceptConnectionHandler implements Handler {
	
	private AtomicInteger aint = new AtomicInteger(0);
	private static AcceptConnectionHandler instance = new AcceptConnectionHandler();
	
	private AcceptConnectionHandler(){
	}
	
	public static AcceptConnectionHandler getInstance(){
		return instance;
	}

	@Override
	public void handleRequest(ServiceContext context, ServiceEvent event)
			throws Exception {
		// TODO Auto-generated method stub
		if(event instanceof ServiceOnAcceptedEvent){
			ServiceOnAcceptedEvent objServiceOnAcceptedEvent = (ServiceOnAcceptedEvent)event; 
			ServerSocketChannel ssc = (ServerSocketChannel) objServiceOnAcceptedEvent.getSelectionKey()
					.channel();
			SelectionKey key = objServiceOnAcceptedEvent.getSelectionKey();
			SocketChannel sc = ssc.accept();
			sc.configureBlocking(false);
			// 将接收到的channel，放到工作线程池中
			fireRegisterChannel(sc);
			System.out.println("Accepted connection ... count = " + aint.incrementAndGet());
		}
	}	
}
