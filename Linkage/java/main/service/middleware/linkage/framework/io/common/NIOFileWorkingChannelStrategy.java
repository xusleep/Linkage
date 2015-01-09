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

import service.middleware.linkage.framework.common.ConvertUtils;
import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.event.ServiceExeptionEvent;
import service.middleware.linkage.framework.exception.ServiceFileTransferErrorException;
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
	private static final long FILE_TRANSFER_BUFFER_SIZE = 1024 * 1024 * 10;
	private static final int FILE_STATE_OK = 0;
	private static final int FILE_STATE_WRONG = 1;
	
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
			this.setWorkingState(new ClientFreeState(this));
		}
		return writeResult;
	}
	
	
	@Override
	public void clear() {
	}
	
	/**
	 * read the file from the channel
	 * @param objFileInformationEntity
	 * @return
	 */
	protected WorkingChannelOperationResult readFile(FileTransferEntity objFileInformationEntity){
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
			if(fileState != FILE_STATE_OK){
				this.getEventDistributionHandler().submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), null, 
						new ServiceFileTransferErrorException(new Exception("receive file error, wrong state"), "receive file error, wrong state")));
				return this.readAndDrop();
			}
			bb = ByteBuffer.allocate(8);
            while (sc.read(bb) > 0) {
                if (!bb.hasRemaining()) {
                    break;
                }
            }
            fileSize = ConvertUtils.bytesToLong(bb.array());
            bb.clear();
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
			return this.readAndDrop();
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
	
	/**
	 * write the file into the channel
	 * @param objFileInformationEntity
	 * @return
	 */
	protected WorkingChannelOperationResult writeFile(FileTransferEntity objFileInformationEntity){
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
