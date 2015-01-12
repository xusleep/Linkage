package service.middleware.linkage.framework.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.exception.ServiceOnChanelClosedException;
import service.middleware.linkage.framework.exception.ServiceOnChanelIOException;
import service.middleware.linkage.framework.io.nio.strategy.message.NIOMessageWorkingChannelStrategy;
import service.middleware.linkage.framework.io.nio.strategy.message.events.ServiceOnMessageReceiveEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceExeptionEvent;
import service.middleware.linkage.framework.serialization.SerializationUtils;
import service.middleware.linkage.framework.serviceaccess.ServiceAccess;
import service.middleware.linkage.framework.serviceaccess.entity.ResponseEntity;
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
		if (event instanceof ServiceOnMessageReceiveEvent) {
			try {
				ServiceOnMessageReceiveEvent objServiceOnMessageReceiveEvent = (ServiceOnMessageReceiveEvent) event;
				String receiveData = objServiceOnMessageReceiveEvent.getMessage();
				ResponseEntity objResponseEntity = SerializationUtils.deserializeResponse(receiveData);
				NIOMessageWorkingChannelStrategy strategy = (NIOMessageWorkingChannelStrategy)objServiceOnMessageReceiveEvent.getWorkingChannel().getWorkingChannelStrategy();
				strategy.setRequestResult(objResponseEntity);
				if(this.getNext() != null)
				{
					this.getNext().handleRequest(event);
				}
			} catch (Exception e) {
				logger.error("there is an exception comes out: " + StringUtils.ExceptionStackTraceToString(e));
			}
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
