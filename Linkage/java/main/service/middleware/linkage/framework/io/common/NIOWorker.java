package service.middleware.linkage.framework.io.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.distribution.EventDistributionMaster;
import service.middleware.linkage.framework.event.ServiceOnChannelCloseExeptionEvent;
import service.middleware.linkage.framework.event.ServiceOnChannelIOExeptionEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageReceiveEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageWriteEvent;
import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.exception.ServiceOnChanelClosedException;
import service.middleware.linkage.framework.exception.ServiceOnChanelIOException;
import service.middleware.linkage.framework.io.protocol.IOProtocol;

/**
 * this is default worker, will be used when there is connection established 
 * it will deal with the message send&receive between the client and service
 * @author zhonxu
 *
 */
public class NIOWorker implements Worker {
	private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();
	protected final AtomicBoolean wakenUp = new AtomicBoolean();
	private final Selector selector;
	private final CountDownLatch signal;
	private volatile boolean isShutdown = false;
	private final CountDownLatch shutdownSignal;
	private final NIOReadWriteContext readWriteContext;
	private static Logger  logger = Logger.getLogger(NIOWorker.class); 
	private final EventDistributionMaster eventDistributionHandler;

	public NIOWorker(EventDistributionMaster eventDistributionHandler, CountDownLatch signal) throws Exception {
		selector = Selector.open();
		this.signal = signal;
		shutdownSignal = new CountDownLatch(1);
		this.eventDistributionHandler = eventDistributionHandler;
		readWriteContext = new NIOReadWriteContext(eventDistributionHandler);
	}

	public void run() {
		signal.countDown();
		logger.debug("start worker hashCode = " + this.hashCode());
		// Listening
		while (true) {
			try {
				wakenUp.set(false);
				int num = 0;
				num = selector.select();
				// If we shutdown the loop, then beak from the loop
				if(isShutdown){
					logger.debug("shutdown, break loop after select.");
					shutdownSignal.countDown();
					break;
				}
				processTaskQueue();
				if (num > 0) {
					Set selectedKeys = selector.selectedKeys();
					Iterator it = selectedKeys.iterator();
					while (it.hasNext()) {
						SelectionKey key = (SelectionKey) it.next();
						it.remove();
						// 处理IO事件
						if (key.isReadable()) {
							// 这里不能使用 同步方法，
							// 当使用同步方法时，如果传输大文件的话，会阻塞其他通道的数据
							if(!read(key)){
								this.closeChannel(key);
							}
						} else if (key.isWritable()) {
							writeFromSelectorLoop(key);
						} 
					}
				} 
			} catch (IOException e) {
				if(isShutdown){
					logger.debug("shutdown, break loop after ioexception. exception detail : " 
						+ StringUtils.ExceptionStackTraceToString(e));
					shutdownSignal.countDown();
					break;
				}
				logger.error("not expected interruptedException happened. exception detail : " 
						+ StringUtils.ExceptionStackTraceToString(e));
				continue;
			}
		}
	}
	
	/**
	 * get read write context
	 * @return
	 */
	public NIOReadWriteContext getReadWriteContext() {
		return readWriteContext;
	}
	
	/**
	 * shutdown 
	 */
	public void shutdown(){
		logger.debug("shutdown worker.");
		isShutdown = true;
		if (selector != null) {
            if (wakenUp.compareAndSet(false, true)) {
                selector.wakeup();
            }
        }
	}
	
	/**
	 * shutdown 
	 */
	public void shutdownImediate(){
		logger.debug("shutdown worker imediately.");
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
	}
	
	/**
	 * deal with the task queue
	 */
    private void processTaskQueue() {
        for (;;) {
            final Runnable task = taskQueue.poll();
            if (task == null) {
                break;
            }
            task.run();
        }
    }
    
	/**
	 * write when the select event happens
	 * @param key
	 */
    private void writeFromSelectorLoop(final SelectionKey key) {
    	WorkingChannel channel = (WorkingChannel) key.attachment();
    	writeFromUser(channel);
    }
	
	/**
	 * write data by user
	 * @param key
	 * @return
	 */
	public boolean writeFromUser(WorkingChannel workingChannel) {
		return this.getReadWriteContext().write(workingChannel);
	}

	/**
	 * close the channel
	 * 
	 * @param sc
	 * @throws IOException
	 */
	private void closeChannel(SelectionKey key) throws IOException {
		key.cancel();
		SocketChannel sc = (SocketChannel)key.channel();
		sc.close();
	}
	
	/**
	 * close the working channel
	 * @param workingchannel
	 * @throws IOException
	 */
	public void closeWorkingChannel(WorkingChannel workingchannel) throws IOException{
		closeChannel(((NIOWorkingChannel)workingchannel).getKey());
	}
	
	/**
	 *  
	 * @param key
	 * @return
	 */
	private boolean read(SelectionKey key) {
		final NIOWorkingChannel workingChannel = (NIOWorkingChannel)key.attachment();
		return this.getReadWriteContext().read(workingChannel);
	}

	/**
	 * register a channel to the worker
	 */
	public WorkingChannel submitOpeRegister(SocketChannel schannel) {
		WorkingChannel objWorkingChannel = new NIOWorkingChannel(schannel, this);
		registerTask(new RegisterTask(objWorkingChannel));
		return objWorkingChannel;
	}
	
	/**
	 * submit a register task to the task queue
	 * @param task
	 */
    protected final void registerTask(Runnable task) {
        taskQueue.add(task);
        Selector selector = this.selector;
        if (selector != null) {
            if (wakenUp.compareAndSet(false, true)) {
                selector.wakeup();
            }
        } else {
            if (taskQueue.remove(task)) {
                // the selector was null this means the Worker has already been shutdown.
                throw new RejectedExecutionException("Worker has already been shutdown");
            }
        }
    }

	/**
	 * this class is used to register the task to the worker
	 * @author zhonxu
	 *
	 */
	private final class RegisterTask implements Runnable {
		
		private NIOWorkingChannel objWorkingChannel;
		
		public RegisterTask(WorkingChannel workingChannel){
			this.objWorkingChannel = (NIOWorkingChannel)workingChannel;
		}

		@Override
		public void run() {
			SocketChannel schannel = (SocketChannel) objWorkingChannel.getChannel();
			try {
				schannel.configureBlocking(false);
				SelectionKey key = schannel.register(selector, SelectionKey.OP_READ, objWorkingChannel);
				objWorkingChannel.setKey(key);
			} catch (Exception e) {
				try {
					schannel.finishConnect();
					schannel.close();
					schannel.socket().close();
					eventDistributionHandler.submitServiceEvent(new ServiceOnChannelCloseExeptionEvent(objWorkingChannel, null, new ServiceException(e, e.getMessage())));
				} catch (Exception e1) {
				}
			}
		}
		
	}
}
