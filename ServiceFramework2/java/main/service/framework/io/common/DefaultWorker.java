package service.framework.io.common;

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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import service.framework.common.StringUtils;
import service.framework.distribution.EventDistributionMaster;
import service.framework.event.ServiceOnChannelCloseExeptionEvent;
import service.framework.event.ServiceOnChannelIOExeptionEvent;
import service.framework.event.ServiceOnMessageReceiveEvent;
import service.framework.event.ServiceOnMessageWriteEvent;
import service.framework.exception.ServiceException;
import service.framework.exception.ServiceOnChanelClosedException;
import service.framework.exception.ServiceOnChanelIOException;
import service.framework.io.protocol.ShareingProtocolData;
import service.framework.io.server.DefaultServer;

/**
 * <p>
 * Title: 主控服务线程 主要用于，建立与客户端的连接
 * </p>
 * 
 * @author starboy
 * @version 1.0
 */

public class DefaultWorker implements Worker {
	private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();
	protected final AtomicBoolean wakenUp = new AtomicBoolean();
	private final Selector selector;
	private final ExecutorService objExecutorService;
	private final CountDownLatch signal;
	private final EventDistributionMaster eventDistributionHandler;
	private volatile boolean isShutdown = false;
	private final CountDownLatch shutdownSignal;
	private static Logger  logger = Logger.getLogger(DefaultWorker.class);  
	
	public DefaultWorker(EventDistributionMaster eventDistributionHandler, CountDownLatch signal) throws Exception {
		// 创建无阻塞网络套接
		selector = Selector.open();
		this.objExecutorService = Executors.newFixedThreadPool(10);
		this.signal = signal;
		this.eventDistributionHandler = eventDistributionHandler;
		shutdownSignal = new CountDownLatch(1);
	}

	public void run() {
		signal.countDown();
		logger.debug("start worker hashCode = " + this.hashCode());
		// 监听
		while (true) {
			try {
				wakenUp.set(false);
				int num = 0;
				num = selector.select();
				processTaskQueue();
				if (num > 0) {
					Set selectedKeys = selector.selectedKeys();
					Iterator it = selectedKeys.iterator();
					while (it.hasNext()) {
						SelectionKey key = (SelectionKey) it.next();
						it.remove();
						// 处理IO事件
						if (key.isReadable()) {
							if(!read(key)){
								this.closeChannel(key);
							}
						} else if (key.isWritable()) {
							writeFromSelectorLoop(key);
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
	
	/**
	 * 处理内部任务队列
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
	
    private void writeFromSelectorLoop(final SelectionKey key) {
    	WorkingChannel channel = (WorkingChannel) key.attachment();
    	writeFromUser(channel);
    }
	
	/**
	 * 写入数据
	 * @param key
	 * @return
	 */
	public boolean writeFromUser(WorkingChannel channel) {
		ServiceOnMessageWriteEvent evt;
		final Queue<ServiceOnMessageWriteEvent> writeBuffer = channel.writeBufferQueue;
		synchronized (channel.writeLock) {
			for (;;) {
				if ((evt = writeBuffer.poll()) == null) {
					break;
				}
				SocketChannel sc = (SocketChannel) channel.getChannel();
				byte[] data = null;
				try {
					data = WorkingChannel.wrapMessage(evt.getMessage())
							.getBytes(ShareingProtocolData.FRAMEWORK_IO_ENCODING);
				} catch (UnsupportedEncodingException e2) {
					e2.printStackTrace();
				}
				ByteBuffer buffer = ByteBuffer
						.allocate(data.length);
				buffer.put(data, 0, data.length);
				buffer.flip();
				if (buffer.hasRemaining()) {
					try {
						while(buffer.hasRemaining())
							sc.write(buffer);
					} catch (IOException e) {
						this.eventDistributionHandler.submitEventPool(new ServiceOnChannelCloseExeptionEvent(channel, evt.getRequestID(), new ServiceException(e, e.getMessage())));
						e.printStackTrace();
						try {
							closeChannel(channel.getKey());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * 设置key的读状态
	 * 
	 * @param key
	 */
	protected void setOpWrite(SelectionKey key) {
		key.interestOps(SelectionKey.OP_READ);
	}

	/**
	 * 关闭通道
	 * 
	 * @param sc
	 * @throws IOException
	 */
	private void closeChannel(SelectionKey key ) throws IOException {
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
		closeChannel(workingchannel.getKey());
	}
	
	/**
	 *  
	 * @param key
	 * @return
	 */
	private boolean read(SelectionKey key) {
		SocketChannel ch = (SocketChannel) key.channel();
		final WorkingChannel objWorkingChannel = (WorkingChannel)key.attachment();
		int readBytes = 0;
		int ret = 0;
		boolean success = false;
	    ByteBuffer bb = ByteBuffer.allocate(ShareingProtocolData.BUFFER_SIZE);
        try {
            while ((ret = ch.read(bb)) > 0) {
                readBytes += ret;
                if (!bb.hasRemaining()) {
                    break;
                }
            }
            if (ret < 0 ) {
            	return success = false;
            }
            success = true;
        } catch (ClosedChannelException e) {
        	this.eventDistributionHandler.submitEventPool(new ServiceOnChannelCloseExeptionEvent(objWorkingChannel, null, new ServiceOnChanelClosedException(e, e.getMessage())));
            return false;
        	// Can happen, and does not need a user attention.
        } catch (IOException e) {
        	this.eventDistributionHandler.submitEventPool(new ServiceOnChannelIOExeptionEvent(objWorkingChannel, null, new ServiceOnChanelIOException(e, e.getMessage())));
        	return false;
        } 
        if (readBytes > 0) {
            bb.flip();
        }
		byte[] message = new byte[readBytes];
		System.arraycopy(bb.array(), 0, message, 0, readBytes);
		String receiveString = "";
		try {
			receiveString = new String(message, ShareingProtocolData.FRAMEWORK_IO_ENCODING);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		synchronized(objWorkingChannel.readLock){
			objWorkingChannel.appendMessage(receiveString);
			String unwrappedMessage = "";
			try {
				while((unwrappedMessage = objWorkingChannel.extractMessage()) != "")
				{
					final String sendMessage = unwrappedMessage;
					objExecutorService.execute(new Runnable(){
	
						@Override
						public void run() {
							ServiceOnMessageReceiveEvent event = new ServiceOnMessageReceiveEvent(objWorkingChannel);
							event.setMessage(sendMessage);
							//System.out.println("fired message ... " + sendMessage);
							eventDistributionHandler.submitEventPool(event);
						}
						
					});
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("exception : " + e.getMessage());
			}
		}
		return success;
	}

	/**
	 * 数组扩容
	 * 
	 * @param src
	 *            byte[] 源数组数据
	 * @param size
	 *            int 扩容的增加量
	 * @return byte[] 扩容后的数组
	 */
	public static byte[] grow(byte[] src, int size) {
		byte[] tmp = new byte[src.length + size];
		System.arraycopy(src, 0, tmp, 0, src.length);
		return tmp;
	}

	/**
	 * 提交新的客户端写请求于主服务线程的回应池中
	 */
	public WorkingChannel submitOpeRegister(SocketChannel schannel) {
		WorkingChannel objWorkingChannel = new WorkingChannel(schannel, this);
		registerTask(new RegisterTask(objWorkingChannel));
		return objWorkingChannel;
	}
	
	/**
	 * 注册任务，主要是将通道注册到selector上
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
	
	private final class RegisterTask implements Runnable {
		
		private WorkingChannel objWorkingChannel;
		
		public RegisterTask(WorkingChannel workingChannel){
			this.objWorkingChannel = workingChannel;
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
					eventDistributionHandler.submitEventPool(new ServiceOnChannelCloseExeptionEvent(objWorkingChannel, null, new ServiceException(e, e.getMessage())));
				} catch (Exception e1) {
				}
			}
		}
		
	}
}
