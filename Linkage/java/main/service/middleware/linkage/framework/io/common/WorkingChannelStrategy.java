package service.middleware.linkage.framework.io.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.List;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.event.ServiceExeptionEvent;
import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.exception.ServiceOnChanelClosedException;
import service.middleware.linkage.framework.exception.ServiceOnChanelIOException;
import service.middleware.linkage.framework.handlers.EventDistributionMaster;
import service.middleware.linkage.framework.io.protocol.IOProtocol;

/**
 * the interface of the the working channel strategy
 * @author zhonxu
 *
 */
public abstract class WorkingChannelStrategy implements WorkingChannelReadWrite{
	
	private final StringBuffer readMessageBuffer;
	private final NIOWorkingChannelContext workingChannelContext;
	private final EventDistributionMaster eventDistributionHandler;
	private final long FILE_TRANSFER_BUFFER_SIZE = 1024 * 1024 * 10;
	private static Logger  logger = Logger.getLogger(WorkingChannelStrategy.class);
	
	public WorkingChannelStrategy(NIOWorkingChannelContext workingChannelContext, EventDistributionMaster eventDistributionHandler){
		this.readMessageBuffer = new StringBuffer(IOProtocol.RECEIVE_BUFFER_MESSAGE_SIZE);
		this.workingChannelContext = workingChannelContext;
		this.eventDistributionHandler = eventDistributionHandler;
	}
	
	/**
	 * read messages from the channel
	 * @param readLock
	 * @return WorkingChannelOperationResult
	 * @throws ServiceException
	 */
	protected WorkingChannelOperationResult readMessages(List<String> extractMessages) {
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
            	return new WorkingChannelOperationResult(false);
            }
        } catch (ClosedChannelException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.eventDistributionHandler.submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceOnChanelClosedException(e, e.getMessage())));
			this.getWorkingChannelContext().closeWorkingChannel();
			return new WorkingChannelOperationResult(false);
        } 
        // Can happen, and does not need a user attention.
        catch (IOException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.eventDistributionHandler.submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceOnChanelIOException(e, e.getMessage())));
			this.getWorkingChannelContext().closeWorkingChannel();
			return new WorkingChannelOperationResult(false);
        } 
        if (readBytes > 0) {
            bb.flip();
        }
		byte[] message = new byte[readBytes];
		System.arraycopy(bb.array(), 0, message, 0, readBytes);
		String receiveString = "";
		try {
			receiveString = new String(message, IOProtocol.FRAMEWORK_IO_ENCODING);
			logger.debug("receiveString : " + receiveString);
		} catch (UnsupportedEncodingException e1) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e1));
			this.eventDistributionHandler.submitServiceEvent(
					new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceException(e1, e1.getMessage())));
			return new WorkingChannelOperationResult(true);
		}
		this.getReadMessageBuffer().append(receiveString);
		String unwrappedMessage = "";
		try {
			while((unwrappedMessage = IOProtocol.extractMessage(this.getReadMessageBuffer())) != "")
			{
				extractMessages.add(unwrappedMessage);
			}
			
		} catch (Exception e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.eventDistributionHandler.submitServiceEvent(
					new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceException(e, e.getMessage())));
			return new WorkingChannelOperationResult(true);
		}
		return new WorkingChannelOperationResult(true);
	}
	
	/**
	 * write the message into the channel
	 * @param message
	 * @return
	 * @throws ServiceException
	 */
	protected WorkingChannelOperationResult writeMessage(String message) {
		SocketChannel sc = (SocketChannel) this.getWorkingChannelContext().getChannel();
		byte[] data = null;
		try {
			data = IOProtocol.wrapMessage(message).getBytes(IOProtocol.FRAMEWORK_IO_ENCODING);
		} catch (UnsupportedEncodingException e2) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e2));
			this.eventDistributionHandler.submitServiceEvent(
					new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceException(e2, e2.getMessage())));
			return new WorkingChannelOperationResult(true);
		}
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
		buffer.put(data, 0, data.length);
		buffer.flip();
		if (buffer.hasRemaining()) {
			try {
				while(buffer.hasRemaining())
					sc.write(buffer);
			} catch (IOException e) {
				logger.error("not expected interruptedException happened. exception detail : " 
						+ StringUtils.ExceptionStackTraceToString(e));
				this.eventDistributionHandler.submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceOnChanelIOException(e, e.getMessage())));
				this.getWorkingChannelContext().closeWorkingChannel();
				return new WorkingChannelOperationResult(false);
			}
		}
		return new WorkingChannelOperationResult(true);
	}
	
	protected WorkingChannelOperationResult readFile(FileInformationEntity objFileInformationEntity){
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(objFileInformationEntity.getReadFile());
		} catch (FileNotFoundException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(
					new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceException(e, e.getMessage())));
			return new WorkingChannelOperationResult(true);
		}
    	FileChannel fileChannel = fos.getChannel();
    	SocketChannel sc = (SocketChannel) this.getWorkingChannelContext().getChannel();
		try {
			logger.debug("start receiving file. file size : " + objFileInformationEntity.getFileSize() + " bytes");
	    	long readCount = fileChannel.transferFrom(sc, 0, FILE_TRANSFER_BUFFER_SIZE);
	    	long totalCount = readCount;
	    	while(readCount >= 0){
	    		readCount = fileChannel.transferFrom(sc, totalCount, FILE_TRANSFER_BUFFER_SIZE);
	    		totalCount = totalCount + readCount;
	    		logger.debug("received " + totalCount + " bytes");
	    		if(objFileInformationEntity.getFileSize() == totalCount)
	    		{
	    			break;
	    		}
	    		else if(objFileInformationEntity.getFileSize() > totalCount){
	    			break;
	    		}
	    	}
		} catch (IOException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceOnChanelIOException(e, e.getMessage())));
			this.getWorkingChannelContext().closeWorkingChannel();
			return new WorkingChannelOperationResult(false);
		}
		try {
	    	fileChannel.close();
	        fos.close();
		} catch (IOException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(
					new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceException(e, e.getMessage())));
			return new WorkingChannelOperationResult(true);
		}
		return new WorkingChannelOperationResult(true);
	}
	
	protected WorkingChannelOperationResult writeFile(FileInformationEntity objFileInformationEntity){
		SocketChannel sc = (SocketChannel) this.getWorkingChannelContext().getChannel();
		FileInputStream fis;
		try {
			fis = new FileInputStream(objFileInformationEntity.getWriteFile());
		} catch (FileNotFoundException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(
					new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceException(e, e.getMessage())));
			return new WorkingChannelOperationResult(true);
		}
		long offset = 0;
		long totalBytes = objFileInformationEntity.getWriteFile().length();
		logger.debug("start file transfering ... filesize: " + totalBytes);
		FileChannel fileChannel = fis.getChannel();
		try {
			while (offset < totalBytes) {
				long buffSize = FILE_TRANSFER_BUFFER_SIZE; 
				if (totalBytes - offset < buffSize) {
					buffSize = totalBytes - offset;
				}
				long transferred = fileChannel.transferTo(offset, buffSize, sc);
				if (transferred > 0) {
					offset += transferred;
					logger.debug("transfered " + offset + " bytes");
				}
			}
		} catch (IOException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceOnChanelIOException(e, e.getMessage())));
			this.getWorkingChannelContext().closeWorkingChannel();
			return new WorkingChannelOperationResult(false);
		}
		try {
			fileChannel.close();
			fis.close();
		} catch (IOException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(
					new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceException(e, e.getMessage())));
			return new WorkingChannelOperationResult(true);
		}
		return new WorkingChannelOperationResult(true);
	}
	
	public EventDistributionMaster getEventDistributionHandler() {
		return eventDistributionHandler;
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
