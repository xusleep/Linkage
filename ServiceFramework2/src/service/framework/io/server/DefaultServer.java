package service.framework.io.server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import service.framework.io.event.ServiceStartedEvent;
import service.framework.io.event.ServiceStartingEvent;
import service.framework.io.fire.MasterHandler;
import servicecenter.service.ServiceInformation;

/**
 * <p>
 * Title: 主控服务线程
 * 主要用于，建立与客户端的连接
 * </p>
 * 
 * @author starboy
 * @version 1.0
 */

public class DefaultServer implements Server {
	private static Queue<SelectionKey> wpool = new ConcurrentLinkedQueue<SelectionKey>(); // 回应池
	private final Selector selector;
	private final ServerSocketChannel sschannel;
	private final InetSocketAddress address;
	private final WorkerPool workerPool;
	private final MasterHandler objMasterHandler;
	
	public DefaultServer(ServiceInformation serviceInformation, MasterHandler objMasterHandler, WorkerPool workerPool) throws Exception {
		this.objMasterHandler = objMasterHandler;
		objMasterHandler.submitEventPool(new ServiceStartingEvent());
		// 创建无阻塞网络套接
		selector = Selector.open();
		sschannel = ServerSocketChannel.open();
		sschannel.configureBlocking(false);
		System.out.println("Listening to " + serviceInformation.getAddress() + " port : " + serviceInformation.getPort());
		address = new InetSocketAddress(serviceInformation.getAddress(), serviceInformation.getPort());
		ServerSocket ss = sschannel.socket();
		ss.bind(address);
		sschannel.register(selector, SelectionKey.OP_ACCEPT);
		this.workerPool = workerPool;
		objMasterHandler.submitEventPool(new ServiceStartedEvent());
	}

	public WorkerPool getWorkerPool() {
		return workerPool;
	}
	
	

	public MasterHandler getMasterHandler() {
		return objMasterHandler;
	}

	public void run() {
		objMasterHandler.start();
		workerPool.start();
		// 监听
		while (true) {
			try {
				int num = 0;
				num = selector.select();
				if (num > 0) {
					Set selectedKeys = selector.selectedKeys();
					Iterator it = selectedKeys.iterator();
					while (it.hasNext()) {
						SelectionKey key = (SelectionKey) it.next();
						it.remove();
						// 处理IO事件
						if (key.isAcceptable()) {
							// Accept the new connection
							ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
							SocketChannel sc = ssc.accept();
							sc.configureBlocking(false);
							// 将接收到的channel，放到工作线程池中
							this.getWorkerPool().register(sc);
						} 
					}
				} 
			} catch (Exception e) {
				continue;
			}
		}
	}
}
