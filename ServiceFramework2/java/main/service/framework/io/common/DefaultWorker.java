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
import java.util.concurrent.atomic.AtomicLong;

import service.framework.distribution.EventDistributionMaster;
import service.framework.event.ServiceOnExeptionEvent;
import service.framework.event.ServiceOnMessageReceiveEvent;
import service.framework.event.ServiceOnMessageWriteEvent;
import service.framework.exception.ServiceException;
import service.framework.io.protocol.ShareingProtocolData;

/**
 * <p>
 * Title: ���ط����߳� ��Ҫ���ڣ�������ͻ��˵�����
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
	public static AtomicLong readBytesCount = new AtomicLong(0);
	public static AtomicLong writeBytesCount = new AtomicLong(0);
	private final CountDownLatch signal;
	private final EventDistributionMaster eventDistributionHandler;
	
	public DefaultWorker(EventDistributionMaster eventDistributionHandler, CountDownLatch signal) throws Exception {
		// ���������������׽�
		selector = Selector.open();
		this.objExecutorService = Executors.newFixedThreadPool(10);
		this.signal = signal;
		this.eventDistributionHandler = eventDistributionHandler;
	}

	public void run() {
		signal.countDown();
		System.out.println("���� worker id = " + Thread.currentThread().getId());
		// ����
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
						// ����IO�¼�
						if (key.isReadable()) {
							if(!read(key)){
								key.cancel();
								this.closeChannel((SocketChannel) key.channel());
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
	 * �����ڲ��������
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
	 * д������
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
					data = channel.wrapMessage(evt.getMessage())
							.getBytes(ShareingProtocolData.FRAMEWORK_IO_ENCODING);
				} catch (UnsupportedEncodingException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				ByteBuffer buffer = ByteBuffer
						.allocate(data.length);
				buffer.put(data, 0, data.length);
				buffer.flip();
				this.writeBytesCount.getAndAdd(data.length);
				if (buffer.hasRemaining()) {
					try {
						while(buffer.hasRemaining())
							sc.write(buffer);
					} catch (IOException e) {
						this.eventDistributionHandler.submitEventPool(new ServiceOnExeptionEvent(channel, evt.getRequestID(), new ServiceException(e, e.getMessage())));
						// TODO Auto-generated catch block
						e.printStackTrace();
						try {
							closeChannel(sc);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * ����key�Ķ�״̬
	 * 
	 * @param key
	 */
	protected void setOpWrite(SelectionKey key) {
		key.interestOps(SelectionKey.OP_READ);
	}

	/**
	 * �ر�ͨ��
	 * 
	 * @param sc
	 * @throws IOException
	 */
	private void closeChannel(SocketChannel sc) throws IOException {
		sc.close();
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
            success = true;
        } catch (ClosedChannelException e) {
        	e.printStackTrace();
            // Can happen, and does not need a user attention.
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (readBytes > 0) {
            bb.flip();
        }
		byte[] message = new byte[readBytes];
		System.arraycopy(bb.array(), 0, message, 0, readBytes);
		this.readBytesCount.getAndAdd(readBytes);
		String receiveString = "";
		try {
			receiveString = new String(message, ShareingProtocolData.FRAMEWORK_IO_ENCODING);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
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
							// TODO Auto-generated method stub
							ServiceOnMessageReceiveEvent event = new ServiceOnMessageReceiveEvent(objWorkingChannel);
							event.setMessage(sendMessage);
							//System.out.println("fired message ... " + sendMessage);
							eventDistributionHandler.submitEventPool(event);
						}
						
					});
	
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("exception : " + e.getMessage());
			}
		}
		return success;
	}

	/**
	 * ��������
	 * 
	 * @param src
	 *            byte[] Դ��������
	 * @param size
	 *            int ���ݵ�������
	 * @return byte[] ���ݺ������
	 */
	public static byte[] grow(byte[] src, int size) {
		byte[] tmp = new byte[src.length + size];
		System.arraycopy(src, 0, tmp, 0, src.length);
		return tmp;
	}

	/**
	 * �ύ�µĿͻ���д�������������̵߳Ļ�Ӧ����
	 */
	public WorkingChannel submitOpeRegister(SocketChannel schannel) {
		WorkingChannel objWorkingChannel = new WorkingChannel(schannel, this);
		registerTask(new RegisterTask(objWorkingChannel));
		return objWorkingChannel;
	}
	
	/**
	 * ע��������Ҫ�ǽ�ͨ��ע�ᵽselector��
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
			// TODO Auto-generated method stub
			try {
				schannel.configureBlocking(false);
				SelectionKey key = schannel.register(selector, SelectionKey.OP_READ, objWorkingChannel);
				objWorkingChannel.setKey(key);
			} catch (Exception e) {
				try {
					schannel.finishConnect();
					schannel.close();
					schannel.socket().close();
					eventDistributionHandler.submitEventPool(new ServiceOnExeptionEvent(objWorkingChannel, null, new ServiceException(e, e.getMessage())));
				} catch (Exception e1) {
				}
			}
		}
		
	}
}