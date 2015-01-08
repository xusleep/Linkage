package service.middleware.linkage.framework.io.common;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.handlers.EventDistributionMaster;
import service.middleware.linkage.framework.serialization.SerializationUtils;

/**
 * this strategy is only used for the file mode only
 * @author zhonxu
 *
 */
public class NIOFileWorkingChannelStrategy extends WorkingChannelStrategy {
	private final  Queue<File> writeFileQueue = new WriteFileQueue();
	//private Object readWriteLock = new Object();
	private FileInformationEntity currentFileInformationEntity;
	
	public NIOFileWorkingChannelStrategy(NIOWorkingChannelContext nioWorkingChannelContext,
			EventDistributionMaster eventDistributionHandler) {
		super(nioWorkingChannelContext, eventDistributionHandler);
		currentFileInformationEntity = new FileInformationEntity();
		this.currentFileInformationEntity.setRequestFileState(RequestFileState.Free);
	}

	public synchronized boolean offerQueue(File file) {
		return this.writeFileQueue.offer(file);
	}



	@Override
	public synchronized WorkingChannelOperationResult readChannel() {
		// client step 2
		if(currentFileInformationEntity.getRequestFileState() == RequestFileState.Requesting){
			//synchronized(readWriteLock){
				List<String> messages = new LinkedList<String>();
				WorkingChannelOperationResult readResult = this.readMessages(messages);
				if(!readResult.isSuccess())
				{
					return readResult;
				}
				String receiveData = messages.get(0);
				FileInformationEntity objFileInformation = SerializationUtils.deserilizationFileInformationEntity(receiveData);
				if(objFileInformation.getRequestFileState() == RequestFileState.RequestOK){
					WorkingChannelOperationResult writeResult = writeFile(currentFileInformationEntity);
					this.currentFileInformationEntity.setRequestFileState(RequestFileState.Free);
					return writeResult;
				}
				else if(objFileInformation.getRequestFileState() == RequestFileState.Wrong)
				{
					this.currentFileInformationEntity.setRequestFileState(RequestFileState.Free);
					return readResult;
				}
				
			//}
		}
		
		//Server step 1
		if(currentFileInformationEntity.getRequestFileState() == RequestFileState.Free){
			//synchronized(readWriteLock){
				List<String> messages = new LinkedList<String>();
				WorkingChannelOperationResult readResult = this.readMessages(messages);
				if(!readResult.isSuccess())
				{
					return readResult;
				}
				if(messages == null || messages.size() == 0){
					return new WorkingChannelOperationResult(true);
				}
				String receiveData = messages.get(0);
				String responseData;
				FileInformationEntity objFileInformation = SerializationUtils.deserilizationFileInformationEntity(receiveData);
				if(this.currentFileInformationEntity.getRequestFileState() == RequestFileState.Free){
					this.currentFileInformationEntity.setRequestFileState(RequestFileState.RequestOK);
					this.currentFileInformationEntity.setFileName(objFileInformation.getFileName());
					this.currentFileInformationEntity.setFileSize(objFileInformation.getFileSize());
					responseData = SerializationUtils.serilizationFileInformationEntity(this.currentFileInformationEntity);
					return this.writeMessage(responseData);
				}
				else
				{
					objFileInformation.setRequestFileState(RequestFileState.Wrong);
					responseData = SerializationUtils.serilizationFileInformationEntity(objFileInformation);
					this.currentFileInformationEntity.setRequestFileState(RequestFileState.Free);
					//synchronized(readWriteLock){
						return this.writeMessage(responseData);
					//}
				}
			//}
		}
		// server step 2
		if(currentFileInformationEntity.getRequestFileState() == RequestFileState.RequestOK){
			//synchronized(readWriteLock){
				currentFileInformationEntity.setReadFile(new File("D:\\" + currentFileInformationEntity.getFileName()));
				WorkingChannelOperationResult writeResult = readFile(currentFileInformationEntity);
				this.currentFileInformationEntity.setRequestFileState(RequestFileState.Free);
				return writeResult;
			//}
		}
		return new WorkingChannelOperationResult(true);
	}

	@Override
	public synchronized WorkingChannelOperationResult writeChannel() {
			//synchronized(readWriteLock){
				// client step 1
				if(currentFileInformationEntity.getRequestFileState() == RequestFileState.Free)
				{
					File workingFile = null;
					if ((workingFile = writeFileQueue.poll()) == null) {
						return new WorkingChannelOperationResult(true);
					}
					currentFileInformationEntity = new FileInformationEntity();
					currentFileInformationEntity.setWriteFile(workingFile);
					currentFileInformationEntity.setFileName(currentFileInformationEntity.getWriteFile().getName());
					currentFileInformationEntity.setFileSize(currentFileInformationEntity.getWriteFile().length());
					currentFileInformationEntity.setRequestFileState(RequestFileState.Requesting);
					String requestData = SerializationUtils.serilizationFileInformationEntity(currentFileInformationEntity);
					return this.writeMessage(requestData);
				}
			//}
			//try {
			//	Thread.sleep(100);
			//} catch (InterruptedException e) {
			//	e.printStackTrace();
			//}
		//}
		return new WorkingChannelOperationResult(true);
	}
	
	
	@Override
	public void clear() {
		currentFileInformationEntity = new FileInformationEntity();
		currentFileInformationEntity.setRequestFileState(RequestFileState.Free);
	}
	
	/**
	 *  the writting queue for the request
	 * @author zhonxu
	 *
	 */
	private final class WriteFileQueue implements Queue<File> {

        private final Queue<File> queue;

        public WriteFileQueue() {
            queue = new ConcurrentLinkedQueue<File>();
        }

        public File remove() {
            return queue.remove();
        }

        public File element() {
            return queue.element();
        }

        public File peek() {
            return queue.peek();
        }

        public int size() {
            return queue.size();
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }

        public Iterator<File> iterator() {
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

        public boolean addAll(Collection<? extends File> c) {
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

        public boolean add(File e) {
            return queue.add(e);
        }

        public boolean remove(Object o) {
            return queue.remove(o);
        }

        public boolean contains(Object o) {
            return queue.contains(o);
        }

        public boolean offer(File e) {
            boolean success = queue.offer(e);
            return success;
        }

        public File poll() {
        	File e = queue.poll();
            return e;
        }
	}


}
