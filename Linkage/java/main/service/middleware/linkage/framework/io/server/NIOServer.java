package service.middleware.linkage.framework.io.server;

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

import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.handlers.EventDistributionMaster;
import service.middleware.linkage.framework.handlers.NIOMessageEventDistributionMaster;
import service.middleware.linkage.framework.io.common.WorkerPool;
import service.middleware.linkage.framework.io.nio.strategy.WorkingChannelMode;
/**
 * 
 * this class is used the start a server
 * the server will accept the connection and put it into the work pool
 * 
 * @author zhonxu
 *
 */
public class NIOServer implements Server {
	private static Queue<SelectionKey> wpool = new ConcurrentLinkedQueue<SelectionKey>(); 
	private final Selector selector;
	private final ServerSocketChannel sschannel;
	private final InetSocketAddress address;
	private final WorkerPool workerPool;
	private volatile boolean isShutdown = false;
	private final CountDownLatch shutdownSignal;
	protected final AtomicBoolean wakenUp = new AtomicBoolean();
	private static Logger  logger = Logger.getLogger(NIOServer.class);  
	
	public NIOServer(String strAddress, int port, WorkerPool workerPool) throws Exception {
		selector = Selector.open();
		sschannel = ServerSocketChannel.open();
		// set it by no blocking
		sschannel.configureBlocking(false);
		logger.debug("Listening to " + strAddress + " port : " + port);
		address = new InetSocketAddress(strAddress, port);
		ServerSocket ss = sschannel.socket();
		// bind the address and listen to the port
		ss.bind(address);
		sschannel.register(selector, SelectionKey.OP_ACCEPT);
		this.workerPool = workerPool;
		shutdownSignal = new CountDownLatch(1);
	}
	/**
	 * get the current worker pool
	 */
	public WorkerPool getWorkerPool() {
		return workerPool;
	}

	public void run() {
		logger.debug("start the server.");
		workerPool.start();
		workerPool.waitReady();
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
						// accept 
						if (key.isAcceptable()) {
							// Accept the new connection
							ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
							SocketChannel sc = ssc.accept();
							sc.configureBlocking(false);
							// put the accepted channel into the worker pool
							this.getWorkerPool().register(sc, WorkingChannelMode.MIXED);
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
	/**
	 * doing something after the server is shutdown
	 */
	private void doJobAfterShutdown(){
		try {
			sschannel.close();
		} catch (IOException e) {
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
		workerPool.shutdownImediate();
	}

	@Override
	public void waitReady() {
		workerPool.waitReady();
	}
}
