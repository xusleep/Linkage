package service.middleware.linkage.framework.io.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.exception.ServiceOnChanelClosedException;
import service.middleware.linkage.framework.exception.ServiceOnChanelIOException;
import service.middleware.linkage.framework.io.protocol.IOProtocol;

/**
 * the interface of the the working channel strategy
 * @author zhonxu
 *
 */
public abstract class WorkingChannelStrategy implements WorkingChannelReadWrite{
	
	private final StringBuffer readMessageBuffer;
	private final NIOWorkingChannelContext workingChannelContext;
	
	public WorkingChannelStrategy(NIOWorkingChannelContext workingChannelContext){
		this.readMessageBuffer = new StringBuffer(IOProtocol.RECEIVE_BUFFER_MESSAGE_SIZE);
		this.workingChannelContext = workingChannelContext;
	}
	
	/**
	 * read messages from the channel
	 * @param readLock
	 * @return
	 * @throws ServiceException
	 */
	public List<String> readMessages(Object readLock) throws ServiceException {
		List<String> extractMessages = new LinkedList<String>();
		SocketChannel ch = (SocketChannel) this.workingChannelContext.getChannel();
		int readBytes = 0;
		int ret = 0;
	    ByteBuffer bb = ByteBuffer.allocate(IOProtocol.BUFFER_SIZE);
        try {
            while ((ret = ch.read(bb)) > 0) {
                readBytes += ret;
                if (!bb.hasRemaining()) {
                    break;
                }
            }
            if (ret < 0 ) {
            	throw new ServiceOnChanelClosedException(new Exception("Channel is closed normally."), "Channel is closed normally.");
            }
        } catch (ClosedChannelException e) {
        	throw new ServiceOnChanelClosedException(e, e.getMessage());
        } 
        // Can happen, and does not need a user attention.
        catch (IOException e) {
        	throw new ServiceOnChanelIOException(e, e.getMessage());
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
			throw new ServiceException(e1, e1.getMessage());
		}
		synchronized(readLock){
			this.getReadMessageBuffer().append(receiveString);
			String unwrappedMessage = "";
			try {
				while((unwrappedMessage = IOProtocol.extractMessage(this.getReadMessageBuffer())) != "")
				{
					extractMessages.add(unwrappedMessage);
				}
				
			} catch (Exception e) {
				throw new ServiceException(e, e.getMessage());
			}
		}
		return extractMessages;
	}
	
	public boolean writeMessages(String message) throws ServiceException {
		SocketChannel sc = (SocketChannel) this.getWorkingChannelContext().getChannel();
		byte[] data = null;
		try {
			data = IOProtocol.wrapMessage(message).getBytes(IOProtocol.FRAMEWORK_IO_ENCODING);
		} catch (UnsupportedEncodingException e2) {
			throw new ServiceException(e2, e2.getMessage());
		}
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
		buffer.put(data, 0, data.length);
		buffer.flip();
		if (buffer.hasRemaining()) {
			try {
				while(buffer.hasRemaining())
					sc.write(buffer);
			} catch (IOException e) {
				throw new ServiceException(e, e.getMessage());
			}
		}
		return true;
	}
	
	public StringBuffer getReadMessageBuffer() {
		return readMessageBuffer;
	}


	public NIOWorkingChannelContext getWorkingChannelContext() {
		return workingChannelContext;
	}


	/**
	 * this class is used when the WorkingChannelStrategy is not 
	 * used
	 */
	public abstract void clear();
}
