package service.middleware.linkage.framework.io.nio.strategy.message;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.exception.ServiceOnChanelClosedException;
import service.middleware.linkage.framework.exception.ServiceOnChanelIOException;
import service.middleware.linkage.framework.handlers.EventDistributionMaster;
import service.middleware.linkage.framework.io.WorkingChannelContext;
import service.middleware.linkage.framework.io.WorkingChannelOperationResult;
import service.middleware.linkage.framework.io.WorkingChannelStrategy;
import service.middleware.linkage.framework.io.nio.NIOWorkingChannelContext;
import service.middleware.linkage.framework.io.nio.strategy.message.events.ServiceOnMessageReceiveEvent;
import service.middleware.linkage.framework.io.nio.strategy.message.events.ServiceOnMessageWriteEvent;
import service.middleware.linkage.framework.io.nio.strategy.message.protocol.IOProtocol;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceExeptionEvent;
import service.middleware.linkage.framework.serviceaccess.entity.RequestResultEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ResponseEntity;
import service.middleware.linkage.framework.utils.EncodingUtils;
import service.middleware.linkage.framework.utils.StringUtils;

/**
 * this strategy is only used for the message mode only
 * @author zhonxu
 *
 */
public class NIOMessageWorkingChannelStrategy extends WorkingChannelStrategy {

	/**
     * Monitor object for write.
     */
    final public Object writeLock = new Object();
    /**
     * Monitor object for read.
     */
    final public Object readLock = new Object();
    /**
     * Queue of write {@link ServiceOnMessageWriteEvent}s.s
     */
    public final  Queue<ServiceOnMessageWriteEvent> writeBufferQueue = new WriteMessageQueue();
	private final ExecutorService objExecutorService;
	private final EventDistributionMaster eventDistributionHandler;
	private final StringBuffer readMessageBuffer;
	private static Logger  logger = Logger.getLogger(NIOMessageWorkingChannelStrategy.class);
	
	/**
	 *  use the concurrent hash map to store the request result list {@link RequestResultEntity}
	 */
	private final ConcurrentHashMap<String, RequestResultEntity> resultList = new ConcurrentHashMap<String, RequestResultEntity>(2048);
	
	public NIOMessageWorkingChannelStrategy(NIOWorkingChannelContext workingChannelContext, EventDistributionMaster eventDistributionHandler){
		super(workingChannelContext);
		this.eventDistributionHandler = eventDistributionHandler;
		this.readMessageBuffer = new StringBuffer(IOProtocol.RECEIVE_BUFFER_MESSAGE_SIZE);
		this.objExecutorService = Executors.newFixedThreadPool(10);
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
		this.clearAllResult(new ServiceException(null, "clear operations."));
	}
	
	public void offerWriterQueue(ServiceOnMessageWriteEvent serviceOnMessageWriteEvent) {
		this.writeBufferQueue.offer(serviceOnMessageWriteEvent);
	}
	
	/**
	 * put the request result in the result list
	 * @param requestResultEntity
	 */
	public void offerRequestResult(RequestResultEntity requestResultEntity){
		resultList.put(requestResultEntity.getRequestID(), requestResultEntity);
	}
	
	/**
	 * clear the result
	 */
	public void clearAllResult(ServiceException exception){
		Enumeration<String> keyEnumeration = resultList.keys();
		while(keyEnumeration.hasMoreElements())
		{
			String requestID = keyEnumeration.nextElement();
			RequestResultEntity requestResultEntity = resultList.get(requestID);
			setExceptionToRuquestResult(requestResultEntity, exception);
		}
		resultList.clear();
	}
	
	/**
	 * set the request result
	 * @param requestID
	 * @param strResult
	 */
	public static void setExceptionToRuquestResult(RequestResultEntity result, ServiceException serviceException){
		if(result != null)
		{
		    ResponseEntity objResponseEntity = new ResponseEntity();
		    objResponseEntity.setRequestID(result.getRequestID());
		    objResponseEntity.setResult(serviceException.getMessage());
		    result.setException(true);
		    result.setException(serviceException);
			result.setResponseEntity(objResponseEntity);
		}
	}
	
	/**
	 * set the request result
	 * @param requestID
	 * @param strResult
	 */
	public void setRuquestResult(String requestID, String strResult){
		RequestResultEntity result = this.resultList.remove(requestID);
		if(result != null)
		{
		    ResponseEntity objResponseEntity = new ResponseEntity();
		    objResponseEntity.setRequestID(requestID);
		    objResponseEntity.setResult(strResult);
		    result.setException(false);
			result.setResponseEntity(objResponseEntity);
		}
	}
	
	/**
	 * set the request result
	 * @param requestID
	 * @param strResult
	 */
	public void setExceptionRuquestResult(String requestID, ServiceException serviceException){
		RequestResultEntity result = this.resultList.remove(requestID);
		if(result != null)
		{
		    ResponseEntity objResponseEntity = new ResponseEntity();
		    objResponseEntity.setRequestID(requestID);
		    objResponseEntity.setResult(serviceException.getMessage());
		    result.setException(true);
		    result.setException(serviceException);
			result.setResponseEntity(objResponseEntity);
		}
	}
	
	/**
	 * when the response comes, use this method to set it. 
	 * @param objResponseEntity
	 */
	public RequestResultEntity setRequestResult(ResponseEntity objResponseEntity){
		RequestResultEntity result = this.resultList.remove(objResponseEntity.getRequestID());
		if(result != null)
		{
			result.setException(false);
			result.setResponseEntity(objResponseEntity);
		}
		return result;
	}
	
	
	@Override
	public WorkingChannelOperationResult readChannel() {
		List<String> extractMessages = new LinkedList<String>();
		WorkingChannelOperationResult readResult = new WorkingChannelOperationResult(false);
		synchronized(this.readLock)
		{
			readResult = this.readMessages(extractMessages);
		}

		for(String message: extractMessages)
		{
			final String sendMessage = message;
			final WorkingChannelContext passWorkingChannelContext = this.getWorkingChannelContext();
			final EventDistributionMaster eventDistributionHandler = this.getEventDistributionHandler();
			objExecutorService.execute(new Runnable(){
				@Override
				public void run() {
					ServiceOnMessageReceiveEvent event = new ServiceOnMessageReceiveEvent(passWorkingChannelContext);
					event.setMessage(sendMessage);
					eventDistributionHandler.submitServiceEvent(event);
				}
				
			});
		}
		return readResult;
	}

	@Override
	public WorkingChannelOperationResult writeChannel() {
		ServiceOnMessageWriteEvent evt;
		final Queue<ServiceOnMessageWriteEvent> writeBuffer = this.writeBufferQueue;
		synchronized (this.writeLock) {
			for (;;) {
				if ((evt = writeBuffer.poll()) == null) {
					break;
				}
				WorkingChannelOperationResult writeResult = this.writeMessage(evt.getMessage());
				if(!writeResult.isSuccess())
					return writeResult;
			}
		}
		return new WorkingChannelOperationResult(true);
	}

	/**
	 *  the writting queue for the request
	 * @author zhonxu
	 *
	 */
	private final class WriteMessageQueue implements Queue<ServiceOnMessageWriteEvent> {

        private final Queue<ServiceOnMessageWriteEvent> queue;

        public WriteMessageQueue() {
            queue = new ConcurrentLinkedQueue<ServiceOnMessageWriteEvent>();
        }

        public ServiceOnMessageWriteEvent remove() {
            return queue.remove();
        }

        public ServiceOnMessageWriteEvent element() {
            return queue.element();
        }

        public ServiceOnMessageWriteEvent peek() {
            return queue.peek();
        }

        public int size() {
            return queue.size();
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }

        public Iterator<ServiceOnMessageWriteEvent> iterator() {
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

        public boolean addAll(Collection<? extends ServiceOnMessageWriteEvent> c) {
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

        public boolean add(ServiceOnMessageWriteEvent e) {
            return queue.add(e);
        }

        public boolean remove(Object o) {
            return queue.remove(o);
        }

        public boolean contains(Object o) {
            return queue.contains(o);
        }

        public boolean offer(ServiceOnMessageWriteEvent e) {
            boolean success = queue.offer(e);
            return success;
        }

        public ServiceOnMessageWriteEvent poll() {
        	ServiceOnMessageWriteEvent e = queue.poll();
            return e;
        }
	}
}
