package service.middleware.linkage.framework.io.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.event.ServiceExeptionEvent;
import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.exception.ServiceOnChanelIOException;
import service.middleware.linkage.framework.handlers.EventDistributionMaster;

/**
 * this strategy is only used for the file mode only
 * @author zhonxu
 *
 */
public class NIOFileWorkingChannelStrategy extends WorkingChannelStrategy {
	private final Queue<FileTransferEntity> downloadFileQueue = new TransferFileQueue();
	//private Object readWriteLock = new Object();
	private State workingState;
	private static Logger logger = Logger.getLogger(NIOFileWorkingChannelStrategy.class);
	private final long FILE_TRANSFER_BUFFER_SIZE = 1024 * 1024 * 10;
	
	public NIOFileWorkingChannelStrategy(NIOWorkingChannelContext nioWorkingChannelContext,
			EventDistributionMaster eventDistributionHandler) {
		super(nioWorkingChannelContext, eventDistributionHandler);
		this.workingState = new ServerAcceptRequestState(this);
	}

	public State getWorkingState() {
		return workingState;
	}

	public void setWorkingState(State workingState) {
		this.workingState = workingState;
	}

	public synchronized boolean offerDownloadFileQueue(FileTransferEntity fileTransferEntity) {
		return this.downloadFileQueue.offer(fileTransferEntity);
	}

	@Override
	public synchronized WorkingChannelOperationResult readChannel() {
		WorkingChannelOperationResult readResult =  this.workingState.execute();
		return readResult;
	}

	@Override
	public synchronized WorkingChannelOperationResult writeChannel() {
		FileTransferEntity fileTransferEntity = downloadFileQueue.poll();
		if(fileTransferEntity == null)
			return new WorkingChannelOperationResult(true);
		this.workingState = new ClientDownloadRequestState(this, fileTransferEntity.getFileGetPath(), fileTransferEntity.getFileSavePath());
		WorkingChannelOperationResult writeResult = this.workingState.execute();
		if(!writeResult.isSuccess()){
			this.setWorkingState(new ClientFreeState());
		}
		return writeResult;
	}
	
	
	@Override
	public void clear() {
	}
	
	protected WorkingChannelOperationResult readFile(FileRequestEntity objFileInformationEntity){
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(new File(objFileInformationEntity.getFileSavePath()));
		} catch (FileNotFoundException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(
					new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceException(e, e.getMessage())));
			// read the data, but drop it
			return readAndDrop();
		}
    	FileChannel fileChannel = fos.getChannel();
    	SocketChannel sc = (SocketChannel) this.getWorkingChannelContext().getChannel();

		try {
			logger.debug("start receiving file. file size : " + objFileInformationEntity.getFileSize() + " bytes");
	    	long readCount = fileChannel.transferFrom(sc, 0, FILE_TRANSFER_BUFFER_SIZE);
	    	long totalCount = readCount;
	    	// if the loop did not read the file for a very long time, then quit
	    	int loopWaitCount = 100000;
	    	while(objFileInformationEntity.getFileSize() > totalCount && loopWaitCount > 0){
	    		readCount = fileChannel.transferFrom(sc, totalCount, FILE_TRANSFER_BUFFER_SIZE);
	    		totalCount = totalCount + readCount;
	    		logger.debug("received " + totalCount + " bytes");
	    		if(readCount == 0){
	    			loopWaitCount--;
	    			try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	    	}
	    	logger.debug("received file. file size : " + objFileInformationEntity.getFileSize() + " bytes");
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
	
	protected WorkingChannelOperationResult readAndDrop(){
		try {
			SocketChannel sc = (SocketChannel) this.getWorkingChannelContext().getChannel();
			ByteBuffer bb = ByteBuffer.allocate(2014);
			long readCount = sc.read(bb);
			while(readCount > 0){
				bb.clear();
				readCount = sc.read(bb);
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
	
	protected WorkingChannelOperationResult writeFile(FileRequestEntity objFileInformationEntity){
		SocketChannel sc = (SocketChannel) this.getWorkingChannelContext().getChannel();
		FileInputStream fis;
		try {
			fis = new FileInputStream(new File(objFileInformationEntity.getFileGetPath()));
		} catch (FileNotFoundException e) {
			logger.error("not expected interruptedException happened. exception detail : " 
					+ StringUtils.ExceptionStackTraceToString(e));
			this.getEventDistributionHandler().submitServiceEvent(
					new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceException(e, e.getMessage())));
			return new WorkingChannelOperationResult(true);
		}
		long offset = 0;
		long totalBytes = objFileInformationEntity.getFileSize();
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
	
	/**
	 *  the writting queue for the request
	 * @author zhonxu
	 *
	 */
	private final class TransferFileQueue implements Queue<FileTransferEntity> {

        private final Queue<FileTransferEntity> queue;

        public TransferFileQueue() {
            queue = new ConcurrentLinkedQueue<FileTransferEntity>();
        }

        public FileTransferEntity remove() {
            return queue.remove();
        }

        public FileTransferEntity element() {
            return queue.element();
        }

        public FileTransferEntity peek() {
            return queue.peek();
        }

        public int size() {
            return queue.size();
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }

        public Iterator<FileTransferEntity> iterator() {
            return queue.iterator();
        }

        public Object[] toArray() {
            return queue.toArray();
        }

        public <T> T[] toArray(T[] a) {
            return queue.toArray(a);
        }

        public boolean containsAll(Collection<?> c) {
            return queue.containsAll(c);
        }

        public boolean addAll(Collection<? extends FileTransferEntity> c) {
            return queue.addAll(c);
        }

        public boolean removeAll(Collection<?> c) {
            return queue.removeAll(c);
        }

        public boolean retainAll(Collection<?> c) {
            return queue.retainAll(c);
        }

        public void clear() {
            queue.clear();
        }

        public boolean add(FileTransferEntity e) {
            return queue.add(e);
        }

        public boolean remove(Object o) {
            return queue.remove(o);
        }

        public boolean contains(Object o) {
            return queue.contains(o);
        }

        public boolean offer(FileTransferEntity e) {
            boolean success = queue.offer(e);
            return success;
        }

        public FileTransferEntity poll() {
        	FileTransferEntity e = queue.poll();
            return e;
        }
	}


}
