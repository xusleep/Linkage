package service.middleware.linkage.framework.io.common;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.handlers.EventDistributionMaster;
import service.middleware.linkage.framework.io.protocol.IOProtocol;

/**
 * this strategy is only used for the file mode only
 * @author zhonxu
 *
 */
public class NIOFileWorkingChannelStrategy extends WorkingChannelStrategy {

	enum FileTransferState{
		Free,
		Requesting,
		RequestOK,
		Transfering,
		TransferOK
	}
	public final  Queue<File> writeFileQueue = new WriteFileQueue();
	private FileTransferState fileTransferState = FileTransferState.Requesting;
	private static Logger  logger = Logger.getLogger(NIOFileWorkingChannelStrategy.class);
	
	public NIOFileWorkingChannelStrategy(NIOWorkingChannelContext nioWorkingChannelContext,
			EventDistributionMaster eventDistributionHandler) {
		super(nioWorkingChannelContext);
	}

	@Override
	public WorkingChannelOperationResult readChannel() {
		// client step 2
		if(fileTransferState == FileTransferState.Requesting){
			if(readMessage() == "RequestOK"){
				fileTransferState = FileTransferState.RequestOK;
				writeChannel();
			}
		}
		//Server step 1
		if(fileTransferState == FileTransferState.Free){
			if(readMessage() == "Requesting"){
				writeMessage("RequestOK");
				fileTransferState =  FileTransferState.Transfering;
			}
		}
		// server step 2
		if(fileTransferState == FileTransferState.Transfering){
			readFile();
		}
		return null;
	}

	@Override
	public WorkingChannelOperationResult writeChannel() {
		// client step 1
		if(fileTransferState == FileTransferState.Free)
		{
			fileTransferState = FileTransferState.Requesting;
			writeMessage("Requesting");
		}
		// client step 3
		if(fileTransferState == FileTransferState.RequestOK){
			fileTransferState = FileTransferState.Transfering;
			writeFile();
			fileTransferState = FileTransferState.Free;
		}
		return null;
	}
	
	private void readFile(){
		
	}
	
	private void writeFile(){
		
	}
	
	private String readMessage(){
		return "";
	}
	
	private void writeMessage(String msg){
		SocketChannel sc = (SocketChannel) this.getWorkingChannelContext().getChannel();
		byte[] data = null;
		try {
			data = IOProtocol.wrapMessage(msg)
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
				logger.error("not expected interruptedException happened. exception detail : " 
						+ StringUtils.ExceptionStackTraceToString(e));
				this.getWorkingChannelContext().closeWorkingChannel();
			}
		}
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
