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
import service.framework.io.protocol.CommunicationProtocol;

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
	private final ExecutorService objExecutorService;
	private final CountDownLatch signal;
	private final EventDistributionMaster eventDistributionHandler;
	private volatile boolean isShutdown = false;
	private final CountDownLatch shutdownSignal;
	private static Logger  logger = Logger.getLogger(NIOWorker.class);  
	
	public NIOWorker(EventDistributionMaster eventDistributionHandler, CountDownLatch signal) throws Exception {
		selector = Selector.open();
		this.objExecutorService = Executors.newFixedThreadPool(10);
		this.signal = signal;
		this.eventDistributionHandler = eventDistributionHandler;
		shutdownSignal = new CountDownLatch(1);
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
		NIOWorkingChannel channel = (NIOWorkingChannel)workingChannel;
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
					data = CommunicationProtocol.wrapMessage(evt.getMessage())
							.getBytes(CommunicationProtocol.FRAMEWORK_IO_ENCODING);
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
						this.eventDistributionHandler.submitServiceEvent(new ServiceOnChannelCloseExeptionEvent(channel, evt.getRequestID(), new ServiceException(e, e.getMessage())));
						logger.error("not expected interruptedException happened. exception detail : " 
								+ StringUtils.ExceptionStackTraceToString(e));
						try {
							closeChannel(channel.getKey());
						} catch (IOException e1) {
							logger.error("not expected interruptedException happened. exception detail : " 
									+ StringUtils.ExceptionStackTraceToString(e1));
						}
					}
				}
			}
		}
		return true;
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
		SocketChannel ch = (SocketChannel) key.channel();
		final NIOWorkingChannel objWorkingChannel = (NIOWorkingChannel)key.attachment();
		int readBytes = 0;
		int ret = 0;
		boolean success = false;
	    ByteBuffer bb = ByteBuffer.allocate(CommunicationProtocol.BUFFER_SIZE);
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
        	this.eventDistributionHandler.submitServiceEvent(new ServiceOnChannelCloseExeptionEvent(objWorkingChannel, null, new ServiceOnChanelClosedException(e, e.getMessage())));
            return false;
        	
        } 
        // Can happen, and does not need a user attention.
        catch (IOException e) {
        	this.eventDistributionHandler.submitServiceEvent(new ServiceOnChannelIOExeptionEvent(objWorkingChannel, null, new ServiceOnChanelIOException(e, e.getMessage())));
        	return false;
        } 
        if (readBytes > 0) {
            bb.flip();
        }
		byte[] message = new byte[readBytes];
		System.arraycopy(bb.array(), 0, message, 0, readBytes);
		String receiveString = "";
		try {
			receiveString = new String(message, CommunicationProtocol.FRAMEWORK_IO_ENCODING);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		synchronized(objWorkingChannel.readLock){
			objWorkingChannel.getBufferMessage().append(receiveString);
			String unwrappedMessage = "";
			try {
				while((unwrappedMessage = CommunicationProtocol.extractMessage(objWorkingChannel.getBufferMessage())) != "")
				{
					final String sendMessage = unwrappedMessage;
					objExecutorService.execute(new Runnable(){
	
						@Override
						public void run() {
							ServiceOnMessageReceiveEvent event = new ServiceOnMessageReceiveEvent(objWorkingChannel);
							event.setMessage(sendMessage);
							eventDistributionHandler.submitServiceEvent(event);
						}
						
					});
				}
				
			} catch (Exception e) {
				logger.error("not expected interruptedException happened. exception detail : " 
						+ StringUtils.ExceptionStackTraceToString(e));
			}
		}
		return success;
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
