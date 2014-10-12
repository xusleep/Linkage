package service.framework.io.server;

import static service.framework.io.fire.Fires.fireCommonEvent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import service.framework.io.event.ServiceOnClosedEvent;
import service.framework.io.event.ServiceOnMessageReceiveEvent;
import service.framework.io.event.ServiceOnMessageWriteEvent;
import service.framework.protocol.ShareingProtocolData;

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

	public DefaultWorker() throws Exception {
		// 创建无阻塞网络套接
		selector = Selector.open();
	}

	public void run() {
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
		// 设置回读状态
		setOpWrite(key);
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
				ByteBuffer buffer = ByteBuffer
						.allocate(evt.getMessage().length);
				buffer.put(evt.getMessage(), 0, evt.getMessage().length);
				buffer.flip();
				if (buffer.hasRemaining()) {
					try {
						while(buffer.hasRemaining())
							sc.write(buffer);
					} catch (IOException e) {
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
	 * 设置key的读状态
	 * 
	 * @param key
	 */
	protected void setOpWrite(SelectionKey key) {
		Selector selector = this.selector;
		int interestOps = SelectionKey.OP_READ;
		if ((interestOps & SelectionKey.OP_WRITE) == 0) {
			interestOps |= SelectionKey.OP_WRITE;
			key.interestOps(interestOps);
		}
	}

	/**
	 * 关闭通道
	 * 
	 * @param sc
	 * @throws IOException
	 */
	private void closeChannel(SocketChannel sc) throws IOException {
		sc.close();
	}



	private boolean read(SelectionKey key) {
		SocketChannel ch = (SocketChannel) key.channel();
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
		ServiceOnMessageReceiveEvent event = new ServiceOnMessageReceiveEvent(
				(WorkingChannel)key.attachment());
		event.setMessage(message);
		System.out.println("reviced ... " + new String(message));
		fireCommonEvent(event);
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
			// TODO Auto-generated method stub
			try {
				schannel.configureBlocking(false);
				schannel.register(selector, SelectionKey.OP_READ, objWorkingChannel);
			} catch (Exception e) {
				try {
					schannel.finishConnect();
					schannel.close();
					schannel.socket().close();
					fireCommonEvent(new ServiceOnClosedEvent());
				} catch (Exception e1) {
				}
			}
		}
		
	}
}
