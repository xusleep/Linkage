package service.middleware.linkage.framework.io.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

public class NIOMessageReadWriteStrategy implements ReadWriteStrategy {
	private final EventDistributionMaster eventDistributionHandler;
	private final ExecutorService objExecutorService;
	private static Logger  logger = Logger.getLogger(NIOMessageReadWriteStrategy.class);
	
	public NIOMessageReadWriteStrategy(EventDistributionMaster eventDistributionHandler){
		this.eventDistributionHandler = eventDistributionHandler;
		this.objExecutorService = Executors.newFixedThreadPool(10);
	}
	
	@Override
	public boolean read(WorkingChannel workingChannel) {
		final NIOWorkingChannel nioWorkingChannel = (NIOWorkingChannel)workingChannel;
		SocketChannel ch = (SocketChannel) nioWorkingChannel.getChannel();
		int readBytes = 0;
		int ret = 0;
		boolean success = false;
	    ByteBuffer bb = ByteBuffer.allocate(IOProtocol.BUFFER_SIZE);
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
        	this.eventDistributionHandler.submitServiceEvent(new ServiceOnChannelCloseExeptionEvent(workingChannel, null, new ServiceOnChanelClosedException(e, e.getMessage())));
            return false;
        	
        } 
        // Can happen, and does not need a user attention.
        catch (IOException e) {
        	this.eventDistributionHandler.submitServiceEvent(new ServiceOnChannelIOExeptionEvent(workingChannel, null, new ServiceOnChanelIOException(e, e.getMessage())));
        	return false;
        } 
        if (readBytes > 0) {
            bb.flip();
        }
		byte[] message = new byte[readBytes];
		System.arraycopy(bb.array(), 0, message, 0, readBytes);
		String receiveString = "";
		try {
			receiveString = new String(message, IOProtocol.FRAMEWORK_IO_ENCODING);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		synchronized(nioWorkingChannel.readLock){
			nioWorkingChannel.getReadMessageBuffer().append(receiveString);
			String unwrappedMessage = "";
			try {
				while((unwrappedMessage = IOProtocol.extractMessage(nioWorkingChannel.getReadMessageBuffer())) != "")
				{
					final String sendMessage = unwrappedMessage;
					objExecutorService.execute(new Runnable(){
						@Override
						public void run() {
							ServiceOnMessageReceiveEvent event = new ServiceOnMessageReceiveEvent(nioWorkingChannel);
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

	@Override
	public boolean write(WorkingChannel workingChannel) {
		final NIOWorkingChannel nioWorkingChannel = (NIOWorkingChannel)workingChannel;
		ServiceOnMessageWriteEvent evt;
		final Queue<ServiceOnMessageWriteEvent> writeBuffer = nioWorkingChannel.writeBufferQueue;
		synchronized (nioWorkingChannel.writeLock) {
			for (;;) {
				if ((evt = writeBuffer.poll()) == null) {
					break;
				}
				SocketChannel sc = (SocketChannel) nioWorkingChannel.getChannel();
				byte[] data = null;
				try {
					data = IOProtocol.wrapMessage(evt.getMessage())
							.getBytes(IOProtocol.FRAMEWORK_IO_ENCODING);
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
						this.eventDistributionHandler.submitServiceEvent(new ServiceOnChannelCloseExeptionEvent(nioWorkingChannel, evt.getRequestID(), new ServiceException(e, e.getMessage())));
						logger.error("not expected interruptedException happened. exception detail : " 
								+ StringUtils.ExceptionStackTraceToString(e));
						try {
							closeChannel(nioWorkingChannel.getKey());
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
}
