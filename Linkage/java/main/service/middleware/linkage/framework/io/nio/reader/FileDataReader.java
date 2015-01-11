package service.middleware.linkage.framework.io.nio.reader;

import java.io.File;
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
import service.middleware.linkage.framework.io.common.NIOFileMessageMixStrategy;
import service.middleware.linkage.framework.io.nio.ContentEntity;
import service.middleware.linkage.framework.io.nio.FileEntity;
import service.middleware.linkage.framework.io.nio.FileInformationEntity;

public class FileDataReader extends ReaderDecorator {

	private static Logger logger = Logger.getLogger(FileDataReader.class);
	private static final long FILE_TRANSFER_BUFFER_SIZE = 1024 * 1024 * 10;
	
	public FileDataReader(ReaderInterface wrappedReader) {
		super(wrappedReader);
	}
	
	@Override
	public boolean read(SocketChannel sc, ContentEntity contentEntity) throws IOException {
		logger.debug("FileReader start.");
		FileEntity fileEntity = (FileEntity)contentEntity;
		ByteBuffer bb = ByteBuffer.allocate(8);
		long readCount = 0;
		long ret = 0;
		long totalCount = 8;
        while (totalCount > readCount) {
        	ret = sc.read(bb);
            if (ret < 0 ) {
            	throw new IOException("channel is closed.");
            }
        	readCount = readCount + ret;
        	logger.debug("FileReader read bytes count : " + readCount);
        }
        if (ret < 0 ) {
        	throw new IOException("channel is closed.");
        }
        long fileID = ConvertUtils.bytesToLong(bb.array());
        bb.clear();
        fileEntity.setFileID(fileID);
        long fileSize = contentEntity.getLength() - 8;
        FileInformationEntity fileInformationEntity = NIOFileMessageMixStrategy.findFileInformationEntity(fileID);
        logger.debug("FileReader fileID:" + fileID + " filesize:" + fileSize + " save path:" + fileInformationEntity.getFilePath());
        FileOutputStream fos = new FileOutputStream(new File(fileInformationEntity.getFilePath()));
		FileChannel fileChannel = fos.getChannel();
		readCount = 0;
		totalCount = fileSize;
    	while(totalCount > readCount){
			long buffSize = FILE_TRANSFER_BUFFER_SIZE; 
			if (totalCount - readCount < buffSize) {
				buffSize = totalCount - readCount;
			}
    		ret = fileChannel.transferFrom(sc, readCount, buffSize);
    		readCount = readCount + ret;
    		logger.debug("FileReader file received " + readCount + " bytes");
    	}
    	fileChannel.close();
        fos.close();
        logger.debug("FileReader end.");
		return true;
	}
	
	/**
	 *  the writting queue for the request
	 * @author zhonxu
	 *
	 */
	private final class FileQueue implements Queue<FileInformationEntity> {

       private final Queue<FileInformationEntity> queue;

       public FileQueue() {
           queue = new ConcurrentLinkedQueue<FileInformationEntity>();
       }

       public FileInformationEntity remove() {
           return queue.remove();
       }

       public FileInformationEntity element() {
           return queue.element();
       }

       public FileInformationEntity peek() {
           return queue.peek();
       }

       public int size() {
           return queue.size();
       }

       public boolean isEmpty() {
           return queue.isEmpty();
       }

       public Iterator<FileInformationEntity> iterator() {
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

       public boolean addAll(Collection<? extends FileInformationEntity> c) {
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

       public boolean add(FileInformationEntity e) {
           return queue.add(e);
       }

       public boolean remove(Object o) {
           return queue.remove(o);
       }

       public boolean contains(Object o) {
           return queue.contains(o);
       }

       public boolean offer(FileInformationEntity e) {
           boolean success = queue.offer(e);
           return success;
       }

       public FileInformationEntity poll() {
    	   FileInformationEntity e = queue.poll();
           return e;
       }
	}
}
