package service.middleware.linkage.framework.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.exception.ServiceOnChanelClosedException;
import service.middleware.linkage.framework.exception.ServiceOnChanelIOException;
import service.middleware.linkage.framework.io.nio.strategy.message.NIOMessageWorkingChannelStrategy;
import service.middleware.linkage.framework.io.nio.strategy.mixed.NIOMixedStrategy;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnFileDataReceivedEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnMessageDataReceivedEvent;
import service.middleware.linkage.framework.serialization.SerializationUtils;
import service.middleware.linkage.framework.serviceaccess.ServiceAccess;
import service.middleware.linkage.framework.serviceaccess.entity.ResponseEntity;
import service.middleware.linkage.framework.utils.EncodingUtils;
import service.middleware.linkage.framework.utils.StringUtils;

/**
 * the default handler for the client message received event
 * 
 * 
 * @author zhonxu
 *
 */
public class AccessClientHandler extends Handler {
	private static Logger  logger = Logger.getLogger(AccessClientHandler.class);  
	
	private final ServiceAccess serviceAccess;
	
	public AccessClientHandler(ServiceAccess serviceAccess){
		this.serviceAccess = serviceAccess;
	}
	
	@Override
	public void handleRequest(ServiceEvent event) throws IOException {
		if(event instanceof ServiceOnMessageDataReceivedEvent){
			try {
				ServiceOnMessageDataReceivedEvent objServiceOnMessageDataReceivedEvent = (ServiceOnMessageDataReceivedEvent)event;
				String receiveData = new String(objServiceOnMessageDataReceivedEvent.getMessageData(), EncodingUtils.FRAMEWORK_IO_ENCODING);
				logger.debug("ServiceOnMessageDataReceivedEvent receive message : " + receiveData);
				ResponseEntity objResponseEntity = SerializationUtils.deserializeResponse(receiveData);
				NIOMixedStrategy strategy = (NIOMixedStrategy)objServiceOnMessageDataReceivedEvent.getWorkingChannel().getWorkingChannelStrategy();
				strategy.setRequestResult(objResponseEntity);
				if(this.getNext() != null)
				{
					this.getNext().handleRequest(event);
				}
			} catch (Exception e) {
				logger.error("there is an exception comes out: " + StringUtils.ExceptionStackTraceToString(e));
			}
		}
		else if(event instanceof ServiceOnFileDataReceivedEvent){
			ServiceOnFileDataReceivedEvent objServerOnFileDataReceivedEvent = (ServiceOnFileDataReceivedEvent)event;
			logger.debug("ServerOnFileDataReceivedEvent receive message : " + objServerOnFileDataReceivedEvent.getFileID());
		}
		else if(event instanceof ServiceExeptionEvent ){
			ServiceExeptionEvent objServiceOnExeptionEvent = (ServiceExeptionEvent)event;
			logger.error("there is an exception comes out: " + StringUtils.ExceptionStackTraceToString(objServiceOnExeptionEvent.getExceptionHappen()));
			if(objServiceOnExeptionEvent.getExceptionHappen() instanceof ServiceOnChanelClosedException 
					|| objServiceOnExeptionEvent.getExceptionHappen() instanceof ServiceOnChanelIOException)
			{
				NIOMessageWorkingChannelStrategy strategy = (NIOMessageWorkingChannelStrategy)objServiceOnExeptionEvent.getWorkingChannel().getWorkingChannelStrategy();
				this.serviceAccess.removeCachedChannel(objServiceOnExeptionEvent.getWorkingChannel());
				strategy.clearAllResult(objServiceOnExeptionEvent.getExceptionHappen());
			}
			if(this.getNext() != null)
			{
				try {
					this.getNext().handleRequest(event);
				} catch (Exception e) {
					logger.error("there is an exception comes out: " + StringUtils.ExceptionStackTraceToString(e));
				}
			}
		}
	}
}
