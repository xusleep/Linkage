package service.middleware.framework.io.server;

import java.io.IOException;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import service.middleware.framework.common.StringUtils;
import service.middleware.framework.distribution.EventDistributionMaster;
import service.middleware.framework.event.ServiceStartedEvent;
import service.middleware.framework.event.ServiceStartingEvent;
import service.middleware.framework.io.common.WorkerPool;

/**
 * <p>
 * this class is used the start a server
 * the server will accept the connection and put it into the work pool
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
	private final EventDistributionMaster eventDistributionHandler;
	private volatile boolean isShutdown = false;
	private final CountDownLatch shutdownSignal;
	protected final AtomicBoolean wakenUp = new AtomicBoolean();
	private static Logger  logger = Logger.getLogger(DefaultServer.class);  
	
	public DefaultServer(String strAddress, int port, EventDistributionMaster eventDistributionHandler, WorkerPool workerPool) throws Exception {
		this.eventDistributionHandler = eventDistributionHandler;
		eventDistributionHandler.submitServiceEvent(new ServiceStartingEvent());
		// 创建无阻塞网络套接
		selector = Selector.open();
		sschannel = ServerSocketChannel.open();
		sschannel.configureBlocking(false);
		logger.debug("Listening to " + strAddress + " port : " + port);
		address = new InetSocketAddress(strAddress, port);
		ServerSocket ss = sschannel.socket();
		ss.bind(address);
		sschannel.register(selector, SelectionKey.OP_ACCEPT);
		this.workerPool = workerPool;
		shutdownSignal = new CountDownLatch(1);
	}

	public WorkerPool getWorkerPool() {
		return workerPool;
	}
	
	public EventDistributionMaster getMasterHandler() {
		return eventDistributionHandler;
	}

	public void run() {
		logger.debug("start the server.");
		eventDistributionHandler.start();
		workerPool.start();
		workerPool.waitReady();
		eventDistributionHandler.submitServiceEvent(new ServiceStartedEvent());
		// 监听
		while (true) {
			try {
				wakenUp.set(false);
				int num = 0;
				num = selector.select();
				// If we shutdown the loop, then beak from the loop
				if(isShutdown){
					logger.debug("shutdown, break loop after select.");
					shutdownSignal.countDown();
					doJobAfterShutdown();
					break;
				}
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
			} catch (IOException e) {
				if(isShutdown){
					logger.debug("shutdown, break loop after ioexception. exception detail : " 
						+ StringUtils.ExceptionStackTraceToString(e));
					shutdownSignal.countDown();
					doJobAfterShutdown();
					break;
				}
				logger.error("not expected interruptedException happened. exception detail : " 
						+ StringUtils.ExceptionStackTraceToString(e));
				continue;
			}
		}
	}
	
	private void doJobAfterShutdown(){
		try {
			sschannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
		}
	}
	
	/**
	 * shutdown 
	 */
	public void shutdown(){
		logger.debug("shutdown.");
		isShutdown = true;
		if (selector != null) {
            if (wakenUp.compareAndSet(false, true)) {
                selector.wakeup();
            }
        }
		eventDistributionHandler.shutdown();
		workerPool.shutdown();
	}
	
	/**
	 * shutdown 
	 */
	public void shutdownImediate(){
		logger.debug("shutdown imediately.");
		isShutdown = true;
		if (selector != null) {
            if (wakenUp.compareAndSet(false, true)) {
                selector.wakeup();
            }
        }
		try {
			shutdownSignal.await();
		} catch (InterruptedException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
		}
		eventDistributionHandler.shutdownImediate();
		workerPool.shutdownImediate();
	}

	@Override
	public void waitReady() {
		// TODO Auto-generated method stub
		workerPool.waitReady();
	}
}
