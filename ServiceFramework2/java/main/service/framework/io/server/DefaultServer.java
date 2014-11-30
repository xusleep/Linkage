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
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import service.framework.common.StringUtils;
import service.framework.distribution.EventDistributionMaster;
import service.framework.event.ServiceStartedEvent;
import service.framework.event.ServiceStartingEvent;
import service.framework.io.common.WorkerPool;

/**
 * <p>
 * Title: ���ط����߳�
 * ��Ҫ���ڣ�������ͻ��˵�����
 * </p>
 * 
 * @author starboy
 * @version 1.0
 */

public class DefaultServer implements Server {
	private static Queue<SelectionKey> wpool = new ConcurrentLinkedQueue<SelectionKey>(); // ��Ӧ��
	private final Selector selector;
	private final ServerSocketChannel sschannel;
	private final InetSocketAddress address;
	private final WorkerPool workerPool;
	private final EventDistributionMaster eventDistributionHandler;
	private volatile boolean isShutdown = false;
	private final CountDownLatch shutdownSignal;
	private static Logger  logger = Logger.getLogger(DefaultServer.class);  
	
	public DefaultServer(String strAddress, int port, EventDistributionMaster eventDistributionHandler, WorkerPool workerPool) throws Exception {
		this.eventDistributionHandler = eventDistributionHandler;
		eventDistributionHandler.submitEventPool(new ServiceStartingEvent());
		// ���������������׽�
		selector = Selector.open();
		sschannel = ServerSocketChannel.open();
		sschannel.configureBlocking(false);
		System.out.println("Listening to " + strAddress + " port : " + port);
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
		logger.debug("service is starting ...");
		eventDistributionHandler.start();
		workerPool.start();
		workerPool.waitReady();
		eventDistributionHandler.submitEventPool(new ServiceStartedEvent());
		// ����
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
						// ����IO�¼�
						if (key.isAcceptable()) {
							// Accept the new connection
							ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
							SocketChannel sc = ssc.accept();
							sc.configureBlocking(false);
							// �����յ���channel���ŵ������̳߳���
							this.getWorkerPool().register(sc);
						} 
					}
				} 
			} catch (Exception e) {
				continue;
			}
		}
	}
	
	/**
	 * shutdown 
	 */
	public void shutdown(){
		Thread.currentThread().interrupt();
		isShutdown = true;
		eventDistributionHandler.shutdown();
	}
	
	/**
	 * shutdown 
	 */
	public void shutdownImediate(){
		Thread.currentThread().interrupt();
		isShutdown = true;
		try {
			shutdownSignal.await();
		} catch (InterruptedException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
		}
		eventDistributionHandler.shutdownImediate();
	}

	@Override
	public void waitReady() {
		// TODO Auto-generated method stub
		workerPool.waitReady();
	}
}
