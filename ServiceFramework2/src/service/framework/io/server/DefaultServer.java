package service.framework.io.server;

import static service.framework.io.fire.Fires.fireConnectAccept;
import static service.framework.io.fire.Fires.fireCommonEvent;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import service.framework.io.event.ServiceOnAcceptedEvent;
import service.framework.io.event.ServiceStartedEvent;
import service.framework.io.event.ServiceStartingEvent;
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

	public DefaultServer(ServiceInformation serviceInformation) throws Exception {
		fireCommonEvent(new ServiceStartingEvent());
		// 创建无阻塞网络套接
		selector = Selector.open();
		sschannel = ServerSocketChannel.open();
		sschannel.configureBlocking(false);
		System.out.println("Listening to " + serviceInformation.getAddress() + " port : " + serviceInformation.getPort());
		address = new InetSocketAddress(serviceInformation.getAddress(), serviceInformation.getPort());
		ServerSocket ss = sschannel.socket();
		ss.bind(address);
		sschannel.register(selector, SelectionKey.OP_ACCEPT);
		fireCommonEvent(new ServiceStartedEvent());
	}

	public void run() {
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
							fireConnectAccept(new ServiceOnAcceptedEvent(key, this));
						} 
					}
				} 
			} catch (Exception e) {
				continue;
			}
		}
	}
}
