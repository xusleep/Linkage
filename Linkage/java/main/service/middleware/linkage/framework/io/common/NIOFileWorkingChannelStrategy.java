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
	public final  Queue<File> writeFileQueue = new WriteFileQueue();
	private Object readWriteLock = new Object();
	private RequestFileState requestFileState = RequestFileState.Requesting;
	private static Logger  logger = Logger.getLogger(NIOFileWorkingChannelStrategy.class);
	private volatile File currentTransferFile;
	
	public NIOFileWorkingChannelStrategy(NIOWorkingChannelContext nioWorkingChannelContext,
			EventDistributionMaster eventDistributionHandler) {
		super(nioWorkingChannelContext, eventDistributionHandler);
	}

	@Override
	public WorkingChannelOperationResult readChannel() {
		// client step 2
		if(requestFileState == RequestFileState.Requesting){
			synchronized(readWriteLock){
				List<String> messages = new LinkedList<String>();
				WorkingChannelOperationResult readResult = this.readMessages(messages);
				if(!readResult.isSuccess())
				{
					return readResult;
				}
				String receiveData = messages.get(0);
				FileInformationEntity objFileInformation = SerializationUtils.deserilizationFileInformationEntity(receiveData);
				if(objFileInformation.getRequestFileState() == RequestFileState.RequestOK){
					WorkingChannelOperationResult writeResult = writeFile();
					this.requestFileState = RequestFileState.Free;
					return writeResult;
				}
				else if(objFileInformation.getRequestFileState() == RequestFileState.Wrong)
				{
					this.requestFileState = RequestFileState.Free;
				}
				
			}
		}
		//Server step 1
		if(requestFileState == RequestFileState.Free){
			synchronized(readWriteLock){
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
				if(objFileInformation.getRequestFileState() == RequestFileState.Free){
					this.requestFileState = RequestFileState.RequestOK;
					objFileInformation.setRequestFileState(this.requestFileState);
					responseData = SerializationUtils.serilizationFileInformationEntity(objFileInformation);
					return this.writeMessage(responseData);
				}
				else
				{
					objFileInformation.setRequestFileState(RequestFileState.Wrong);
					responseData = SerializationUtils.serilizationFileInformationEntity(objFileInformation);
					this.requestFileState = RequestFileState.Free;
					synchronized(readWriteLock){
						return this.writeMessage(responseData);
					}
				}
			}
		}
		// server step 2
		if(requestFileState == RequestFileState.RequestOK){
			synchronized(readWriteLock){
				WorkingChannelOperationResult writeResult = readFile();
				this.requestFileState = RequestFileState.Free;
				return writeResult;
			}
		}
		return new WorkingChannelOperationResult(true);
	}

	@Override
	public WorkingChannelOperationResult writeChannel() {
		// client step 1
		if(requestFileState == RequestFileState.Free)
		{
			currentTransferFile = writeFileQueue.poll();
			FileInformationEntity objFileInformation = new FileInformationEntity();
			objFileInformation.setFile(currentTransferFile);
			objFileInformation.setFileName(currentTransferFile.getName());
			objFileInformation.setFileSize(currentTransferFile.length());
			objFileInformation.setRequestFileState(requestFileState);
			String requestData = SerializationUtils.serilizationFileInformationEntity(objFileInformation);
			requestFileState = RequestFileState.Requesting;
			synchronized(readWriteLock){
				return this.writeMessage(requestData);
			}
		}
		// client step 3
		if(requestFileState == RequestFileState.RequestOK){
			synchronized(readWriteLock){
				WorkingChannelOperationResult readResult = writeFile();
				requestFileState = RequestFileState.Free;
				return readResult;
			}
		}
		return new WorkingChannelOperationResult(true);
	}

	private WorkingChannelOperationResult readFile(){
		if(this.requestFileState == RequestFileState.RequestOK){
			
		}
		return new WorkingChannelOperationResult(true);
	}
	
	private WorkingChannelOperationResult writeFile(){
		return new WorkingChannelOperationResult(true);
	}
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
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
