package service.middleware.linkage.framework.io.common;

import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.common.entity.RequestResultEntity;
import service.middleware.linkage.framework.common.entity.ResponseEntity;
import service.middleware.linkage.framework.event.ServiceExeptionEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageReceiveEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageWriteEvent;
import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.exception.ServiceOnChanelClosedException;
import service.middleware.linkage.framework.exception.ServiceOnChanelIOException;
import service.middleware.linkage.framework.handlers.EventDistributionMaster;

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
	private final EventDistributionMaster eventDistributionHandler;
	private final ExecutorService objExecutorService;
	private static Logger  logger = Logger.getLogger(NIOMessageWorkingChannelStrategy.class);
	
	/**
	 *  use the concurrent hash map to store the request result list {@link RequestResultEntity}
	 */
	private final ConcurrentHashMap<String, RequestResultEntity> resultList = new ConcurrentHashMap<String, RequestResultEntity>(2048);
	
	public NIOMessageWorkingChannelStrategy(NIOWorkingChannelContext workingChannelContext, EventDistributionMaster eventDistributionHandler){
		super(workingChannelContext);
		this.objExecutorService = Executors.newFixedThreadPool(10);
		this.eventDistributionHandler = eventDistributionHandler;
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
		List<String> extractMessages = null;
		try {
			extractMessages = this.readMessages(this.readLock);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			//this.eventDistributionHandler.submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), null, new ServiceException(e, e.getMessage())));
			return new WorkingChannelOperationResult(false);
		}
		for(String message: extractMessages)
		{
			final String sendMessage = message;
			final WorkingChannelContext passWorkingChannelContext = this.getWorkingChannelContext();
			objExecutorService.execute(new Runnable(){
				@Override
				public void run() {
					ServiceOnMessageReceiveEvent event = new ServiceOnMessageReceiveEvent(passWorkingChannelContext);
					event.setMessage(sendMessage);
					eventDistributionHandler.submitServiceEvent(event);
				}
				
			});
		}
		return new WorkingChannelOperationResult(true);
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
				try {
					this.writeMessages(evt.getMessage());
				} catch (ServiceException e) {
					this.eventDistributionHandler.submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), evt.getRequestID(), e));
					logger.error("not expected interruptedException happened. exception detail : " 
							+ StringUtils.ExceptionStackTraceToString(e));
					if(e instanceof ServiceOnChanelClosedException || e instanceof ServiceOnChanelIOException)
					{
						this.getWorkingChannelContext().closeWorkingChannel();
					}
				}
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
