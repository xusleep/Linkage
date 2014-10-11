package service.framework.io.fire;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import service.framework.io.event.ServiceEvent;
import service.framework.io.event.ServiceOnAcceptedEvent;
import service.framework.io.handlers.AcceptConnectionHandler;
import service.framework.io.server.WorkerPool;

public class Fires
{	
	public static void fireConnectAccept(ServiceOnAcceptedEvent event) throws Exception {
		AcceptConnectionHandler.getInstance().handleRequest(null, event);
	}	
	
	/**
	 * 处理客户请求,管理用户的联结池,并唤醒队列中的线程进行处理
	 */
	public static void fireCommonEvent(ServiceEvent event) {
		System.out.println("putting into the pool ... ");
		MasterHandler.submitEventPool(event);
		System.out.println("MasterHandler.pool.size() = " + MasterHandler.pool.size());
	}
	
	public static void fireRegisterChannel(SocketChannel sc) throws Exception{
		System.out.println("register " + sc.getRemoteAddress());
		WorkerPool.getInstance().register(sc);
	}
}
