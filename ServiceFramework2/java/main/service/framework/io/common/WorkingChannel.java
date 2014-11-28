package service.framework.io.common;

import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ResponseEntity;
import service.framework.event.ServiceOnMessageWriteEvent;
import service.framework.exception.ServiceException;
import service.framework.io.protocol.ShareingProtocolData;


public class WorkingChannel {
	
    /**
     * Monitor object for write.
     */
    final public Object writeLock = new Object();
    /**
     * Monitor object for read.
     */
    final public Object readLock = new Object();
    
    /**
     * Queue of write {@link ServiceOnMessageWriteEvent}s.
     */
    public final  Queue<ServiceOnMessageWriteEvent> writeBufferQueue = new WriteRequestQueue();
	Channel channel;
	private Worker worker;
	private StringBuffer bufferMessage;
	private SelectionKey key;
	private String workingChannelCacheID;
	/**
	 *  use the concurrent hash map to store the request result list {@link RequestResultEntity}
	 */
	private final ConcurrentHashMap<String, RequestResultEntity> resultList = new ConcurrentHashMap<String, RequestResultEntity>(2048);
	
	public WorkingChannel(Channel channel, Worker worker){
		this.channel = channel;
		this.worker = worker;
		this.bufferMessage = new StringBuffer(ShareingProtocolData.RECEIVE_BUFFER_MESSAGE_SIZE);
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

	/**
	 * append the string to the buffer
	 * @param message
	 */
	public void appendMessage(String message){
    	this.bufferMessage.append(message);
    }
    
	/**
	 * 从缓存区解析出消息
	 * @param sb
	 * @return
	 * @throws Exception 
	 */
	public String extractMessage() throws Exception{
		int headStartIndex = this.bufferMessage.indexOf(ShareingProtocolData.MESSAGE_HEADER_START);
		int headEndIndex = bufferMessage.indexOf(ShareingProtocolData.MESSAGE_HEADER_END);
		if(ShareingProtocolData.MESSAGE_HEADER_START.length() > bufferMessage.length())
			return "";
		if(headStartIndex != 0)
		{
			throw new Exception("the received message is not comleted, some message not receive correct");
		}
		//包头不完整说明没有收到完整包
		if(headEndIndex <= 0)
			return "";
		String head = bufferMessage.substring(headStartIndex, headEndIndex + ShareingProtocolData.MESSAGE_HEADER_END.length());
		String bodyLenthStr =  bufferMessage.substring(headStartIndex + ShareingProtocolData.MESSAGE_HEADER_START.length(), 
				headStartIndex + ShareingProtocolData.MESSAGE_HEADER_START.length() + ShareingProtocolData.MESSAGE_HEADER_LENGTH_PART);
		int bodyLenth = Integer.parseInt(bodyLenthStr);
		//包体长度没有到包头中设定的长度
		if(bufferMessage.length() < ShareingProtocolData.MESSAGE_HEADER_START.length() + 
				ShareingProtocolData.MESSAGE_HEADER_LENGTH_PART + ShareingProtocolData.MESSAGE_HEADER_END.length() + bodyLenth)
		{
			return "";
		}
		String messageBody = bufferMessage.substring(headEndIndex + ShareingProtocolData.MESSAGE_HEADER_END.length(), 
				headEndIndex + ShareingProtocolData.MESSAGE_HEADER_END.length() + bodyLenth);
		bufferMessage.delete(headStartIndex, headEndIndex + ShareingProtocolData.MESSAGE_HEADER_END.length() + bodyLenth);
		return messageBody;
	}
	
	/**
	 * 对消息进行包装
	 * @param message
	 * @return
	 */
	public static String wrapMessage(String message){
		return ShareingProtocolData.MESSAGE_HEADER_START + toLengthString(message.length()) + ShareingProtocolData.MESSAGE_HEADER_END + message; 
	}
	
	/**
	 * Convert the number to a string 
	 * 2    -- > 0002
	 * 21   -- > 0021
	 * 2345 -- > 2345
	 * @param length
	 * @return
	 */
	private static String toLengthString(int length){
		String tmp = "" + length;
		int tmpLength = tmp.length();
		for(int i = 0; i < ShareingProtocolData.MESSAGE_HEADER_LENGTH_PART - tmpLength; i++){
			tmp = "0" + tmp;
		}
		return tmp;
	}
	
	public Worker getWorker() {
		return worker;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
    public SelectionKey getKey() {
		return key;
	}

	public void setKey(SelectionKey key) {
		this.key = key;
	}
	
	public String getWoringChannelCacheID() {
		return workingChannelCacheID;
	}

	public void setWorkingChannelCacheID(String workingChannelCacheID) {
		this.workingChannelCacheID = workingChannelCacheID;
	}
	
	/**
	 *  the writting queue for the request
	 * @author zhonxu
	 *
	 */
	private final class WriteRequestQueue implements Queue<ServiceOnMessageWriteEvent> {

        private final Queue<ServiceOnMessageWriteEvent> queue;

        public WriteRequestQueue() {
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
