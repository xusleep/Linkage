package service.framework.io.server;

import static service.framework.io.fire.Fires.fireCommonEvent;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import service.framework.io.event.ServiceOnClosedEvent;
import service.framework.io.event.ServiceOnReadEvent;
/**
 * <p>
 * Title: 主控服务线程
 * 主要用于，建立与客户端的连接
 * </p>
 * 
 * @author starboy
 * @version 1.0
 */

public class DefaultWorker implements Worker {
	// 注册池，所有待注册进来的channel，都放到这里
	private Queue<SocketChannel> wpool = new ConcurrentLinkedQueue<SocketChannel>(); 
	private final Selector selector;

	public DefaultWorker() throws Exception {
		// 创建无阻塞网络套接
		selector = Selector.open();
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
						if (key.isReadable()) {
							fireCommonEvent(new ServiceOnReadEvent(key, this));
							key.cancel();
						} 
						else
						{
							addReadWriterRegister();
						}
					}
				} 
				else
				{
					addReadWriterRegister();
				}
			} catch (Exception e) {
				continue;
			}
		}
	}

	/**
	 * 添加新的通道注册
	 */
	public void addReadWriterRegister() {
		while (!wpool.isEmpty()) {
			SocketChannel schannel = wpool.poll();
			try {
				schannel.register(selector, SelectionKey.OP_READ);
			} catch (Exception e) {
				try {
					schannel.finishConnect();
					schannel.close();
					schannel.socket().close();
					fireCommonEvent(new ServiceOnClosedEvent());
				} 
				catch (Exception e1) {
				}
			}
		}
	}
	
	/**
	 * 提交新的客户端写请求于主服务线程的回应池中
	 */
	public void submitOpeRegister(SocketChannel schannel) {
		System.out.println("submitOpeRegister ...");
		wpool.offer(schannel);
		selector.wakeup(); // 解除selector的阻塞状态，以便注册新的通道
	}
}
