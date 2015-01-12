package service.middleware.linkage.framework.io.nio.strategy.file;

import java.io.File;
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
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.exception.ServiceFileTransferErrorException;
import service.middleware.linkage.framework.exception.ServiceOnChanelClosedException;
import service.middleware.linkage.framework.exception.ServiceOnChanelIOException;
import service.middleware.linkage.framework.handlers.EventDistributionMaster;
import service.middleware.linkage.framework.io.WorkingChannelOperationResult;
import service.middleware.linkage.framework.io.WorkingChannelStrategy;
import service.middleware.linkage.framework.io.nio.NIOWorkingChannelContext;
import service.middleware.linkage.framework.io.nio.strategy.message.protocol.IOProtocol;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceExeptionEvent;
import service.middleware.linkage.framework.utils.ConvertUtils;
import service.middleware.linkage.framework.utils.EncodingUtils;
import service.middleware.linkage.framework.utils.StringUtils;

/**
 * this strategy is only used for the file mode only
 * @author zhonxu
 *
 */
public class NIOFileWorkingChannelStrategy extends WorkingChannelStrategy {
	private State workingState;
	private static Logger logger = Logger.getLogger(NIOFileWorkingChannelStrategy.class);
	private static final long FILE_TRANSFER_BUFFER_SIZE = 1024 * 1024 * 10;
	private static final int FILE_STATE_OK = 0;
	private static final int FILE_STATE_WRONG = 1;
	private final Semaphore requestRestrain = new Semaphore(1);
	private AtomicInteger requestCount = new AtomicInteger(0);
	private final EventDistributionMaster eventDistributionHandler;
	private final StringBuffer readMessageBuffer;
	
	public NIOFileWorkingChannelStrategy(NIOWorkingChannelContext nioWorkingChannelContext,
			EventDistributionMaster eventDistributionHandler) {
		super(nioWorkingChannelContext);
		this.eventDistributionHandler = eventDistributionHandler;
		this.readMessageBuffer = new StringBuffer(IOProtocol.RECEIVE_BUFFER_MESSAGE_SIZE);
		this.workingState = new ServerAcceptRequestState(this);
	}

	public Semaphore getRequestRestrain() {
		return requestRestrain;
	}

	public State getWorkingState() {
		return workingState;
	}

	public void setWorkingState(State workingState) {
		this.workingState = workingState;
	}

	@Override
	public  WorkingChannelOperationResult readChannel() {
		WorkingChannelOperationResult readResult =  this.workingState.execute();
		return readResult;
	}

	@Override
	public  WorkingChannelOperationResult writeChannel() {
		return new WorkingChannelOperationResult(true);
	}
	
	public StringBuffer getReadMessageBuffer() {
		return readMessageBuffer;
	}



	public EventDistributionMaster getEventDistributionHandler() {
		return eventDistributionHandler;
	}



	/**
	 * read messages from the channel
	 * @param readLock
	 * @return WorkingChannelOperationResult
	 * @throws ServiceException
	 */
	protected WorkingChannelOperationResult readMessages(List<String> extractMessages) {
		SocketChannel ch = (SocketChannel) this.getWorkingChannelContext().getChannel();
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
		logger.debug("readMessage total read bytes count : " + readBytes);
		String receiveString = "";
		try {
			receiveString = new String(message, EncodingUtils.FRAMEWORK_IO_ENCODING);
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
		logger.debug("writeMessage : " + message);
		byte[] data = null;
		try {
			data = IOProtocol.wrapMessage(message).getBytes(EncodingUtils.FRAMEWORK_IO_ENCODING);
		} catch (UnsupportedEncodingException e2) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e2));
			this.eventDistributionHandler.submitServiceEvent(
					new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceException(e2, e2.getMessage())));
			return new WorkingChannelOperationResult(true);
		}
		logger.debug("writeMessage total write bytes count : " + data.length);
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
	
	
	@Override
	public void clear() {
	}
	
	public WorkingChannelOperationResult uploadFile(String fromPath, String toPath){
		try {
			this.getRequestRestrain().acquire();
			logger.debug("client upload acquired access role ... request count : " + requestCount.incrementAndGet());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.workingState = new ClientUploadRequestState(this, fromPath, toPath);
		WorkingChannelOperationResult writeResult = this.workingState.execute();
		if(!writeResult.isSuccess()){
			this.setWorkingState(new ClientFreeState(this));
		}
		return writeResult;
	}
	
	public WorkingChannelOperationResult downloadFile(String fromPath, String toPath){
		try {
			this.getRequestRestrain().acquire();
			logger.debug("client upload acquired access role ... request count : " + requestCount.incrementAndGet());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.workingState = new ClientDownloadRequestState(this, fromPath, toPath);
		WorkingChannelOperationResult writeResult = this.workingState.execute();
		if(!writeResult.isSuccess()){
			this.setWorkingState(new ClientFreeState(this));
		}
		return writeResult;
	}
	
	/**
	 * read the file from the channel
	 * @param objFileInformationEntity
	 * @return
	 */
	public WorkingChannelOperationResult readFile(FileTransferEntity objFileInformationEntity){
		int fileState = 0;
		long fileSize = 0;
    	SocketChannel sc = (SocketChannel) this.getWorkingChannelContext().getChannel();
		try {
			ByteBuffer bb = ByteBuffer.allocate(1);
			while (sc.read(bb) > 0) {
                if (!bb.hasRemaining()) {
                    break;
                }
            }
			fileState = ConvertUtils.byteToInt(bb.get(0));
			bb.clear();
			bb = ByteBuffer.allocate(8);
            while (sc.read(bb) > 0) {
                if (!bb.hasRemaining()) {
                    break;
                }
            }
            fileSize = ConvertUtils.bytesToLong(bb.array());
            bb.clear();
			if(fileState != FILE_STATE_OK){
				this.getEventDistributionHandler().submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), null, 
						new ServiceFileTransferErrorException(new Exception("receive file error, wrong state"), "receive file error, wrong state")));
				return this.readAndDrop(fileSize);
			}
			logger.debug("start receiving file. file size : " + fileSize + " bytes . file state : " + fileState);

			FileOutputStream fos = new FileOutputStream(new File(objFileInformationEntity.getFileSavePath()));
			FileChannel fileChannel = fos.getChannel();
	    	long readCount = fileChannel.transferFrom(sc, 0, FILE_TRANSFER_BUFFER_SIZE);
	    	long totalCount = readCount;
	    	while(fileSize > totalCount){
	    		readCount = fileChannel.transferFrom(sc, totalCount, FILE_TRANSFER_BUFFER_SIZE);
	    		totalCount = totalCount + readCount;
	    		logger.debug("received " + totalCount + " bytes");
	    	}
	    	fileChannel.close();
	        fos.close();
	    	logger.debug("received file. file size : " + fileSize + " bytes");
		}
		catch(FileNotFoundException e){
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceFileTransferErrorException(e, e.getMessage())));
			// read the data, but drop them all
			return this.readAndDrop(fileSize);
		}
		catch (IOException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceOnChanelIOException(e, e.getMessage())));
			this.getWorkingChannelContext().closeWorkingChannel();
			return new WorkingChannelOperationResult(false);
		}
		return new WorkingChannelOperationResult(true);
	}

	/**
	 * read the data from the check, in case it will cause the 
	 * dead loop if we dont read the data
	 * @return
	 */
	protected WorkingChannelOperationResult readAndDrop(long dropCount){
		try {
			SocketChannel sc = (SocketChannel) this.getWorkingChannelContext().getChannel();
			ByteBuffer bb = ByteBuffer.allocate((int) FILE_TRANSFER_BUFFER_SIZE);
			logger.debug("start drop bytes . drop total count : " + dropCount);
			long readCount = sc.read(bb);
			while(readCount < dropCount){
				bb.clear();
				readCount += sc.read(bb);
				logger.debug("dropped bytes . drop count : " + readCount);
			}
		}
		catch (IOException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceOnChanelIOException(e, e.getMessage())));
			this.getWorkingChannelContext().closeWorkingChannel();
			return new WorkingChannelOperationResult(false);
		}
		return new WorkingChannelOperationResult(true);
	}
	
	/**
	 * write the file into the channel
	 * @param objFileInformationEntity
	 * @return
	 */
	public WorkingChannelOperationResult writeFile(FileTransferEntity objFileInformationEntity){
		int fileState = FILE_STATE_OK;
		long fileSize = 0;
		SocketChannel sc = (SocketChannel) this.getWorkingChannelContext().getChannel();
		FileInputStream fis = null;
		FileChannel fileChannel = null;
		try {
			File file = new File(objFileInformationEntity.getFileGetPath());
			fileSize = file.length();
			fis = new FileInputStream(file);
			fileChannel = fis.getChannel();
		} catch (FileNotFoundException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(
					new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceFileTransferErrorException(e, e.getMessage())));
			fileState = FILE_STATE_WRONG;
		}
		
		logger.debug("start writting file. file size : " + fileSize + " bytes . file state : " + fileState);
		try {
			ByteBuffer bb = ByteBuffer.allocate(1);
			bb.put(ConvertUtils.intToByte(fileState));
			bb.flip();
			while(bb.hasRemaining())
				sc.write(bb);
			bb = ByteBuffer.allocate(8);
			if(fileState == FILE_STATE_WRONG)
				fileSize = 0;
			byte[] dst = ConvertUtils.longToBytes(fileSize);
			bb.put(dst, 0, 8);
			bb.flip();
			while(bb.hasRemaining())
				sc.write(bb);
			long offset = 0;
			while (offset < fileSize) {
				long buffSize = FILE_TRANSFER_BUFFER_SIZE; 
				if (fileSize - offset < buffSize) {
					buffSize = fileSize - offset;
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
			if(fileChannel != null)
				fileChannel.close();
			if(fis != null)
				fis.close();
		} catch (IOException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(
					new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceFileTransferErrorException(e, e.getMessage())));
			return new WorkingChannelOperationResult(true);
		}
		return new WorkingChannelOperationResult(true);
	}
	
//	/**
//	 *  the writting queue for the request
//	 * @author zhonxu
//	 *
//	 */
//	private final class TransferFileQueue implements Queue<FileTransferEntity> {
//
//        private final Queue<FileTransferEntity> queue;
//
//        public TransferFileQueue() {
//            queue = new ConcurrentLinkedQueue<FileTransferEntity>();
//        }
//
//        public FileTransferEntity remove() {
//            return queue.remove();
//        }
//
//        public FileTransferEntity element() {
//            return queue.element();
//        }
//
//        public FileTransferEntity peek() {
//            return queue.peek();
//        }
//
//        public int size() {
//            return queue.size();
//        }
//
//        public boolean isEmpty() {
//            return queue.isEmpty();
//        }
//
//        public Iterator<FileTransferEntity> iterator() {
//            return queue.iterator();
//        }
//
//        public Object[] toArray() {
//            return queue.toArray();
//        }
//
//        public <T> T[] toArray(T[] a) {
//            return queue.toArray(a);
//        }
//
//        public boolean containsAll(Collection<?> c) {
//            return queue.containsAll(c);
//        }
//
//        public boolean addAll(Collection<? extends FileTransferEntity> c) {
//            return queue.addAll(c);
//        }
//
//        public boolean removeAll(Collection<?> c) {
//            return queue.removeAll(c);
//        }
//
//        public boolean retainAll(Collection<?> c) {
//            return queue.retainAll(c);
//        }
//
//        public void clear() {
//            queue.clear();
//        }
//
//        public boolean add(FileTransferEntity e) {
//            return queue.add(e);
//        }
//
//        public boolean remove(Object o) {
//            return queue.remove(o);
//        }
//
//        public boolean contains(Object o) {
//            return queue.contains(o);
//        }
//
//        public boolean offer(FileTransferEntity e) {
//            boolean success = queue.offer(e);
//            return success;
//        }
//
//        public FileTransferEntity poll() {
//        	FileTransferEntity e = queue.poll();
//            return e;
//        }
//	}
}
