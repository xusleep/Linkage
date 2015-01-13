package service.middleware.linkage.framework.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.exception.ServiceOnChanelClosedException;
import service.middleware.linkage.framework.exception.ServiceOnChanelIOException;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnFileDataReceivedEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnMessageDataReceivedEvent;
import service.middleware.linkage.framework.serialization.SerializationUtils;
import service.middleware.linkage.framework.serviceaccess.ServiceAccess;
import service.middleware.linkage.framework.serviceaccess.ServiceEngineInterface;
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
	
	private final ServiceEngineInterface serviceAccessEngine;
	
	public AccessClientHandler(ServiceEngineInterface serviceAccessEngine){
		this.serviceAccessEngine = serviceAccessEngine;
	}
	
	@Override
	public void handleRequest(ServiceEvent event) throws IOException {
		if(event instanceof ServiceOnMessageDataReceivedEvent){
			ServiceOnMessageDataReceivedEvent objServiceOnMessageDataReceivedEvent = (ServiceOnMessageDataReceivedEvent)event;
			String receiveData = new String(objServiceOnMessageDataReceivedEvent.getMessageData(), EncodingUtils.FRAMEWORK_IO_ENCODING);
			logger.debug("ServiceOnMessageDataReceivedEvent receive message : " + receiveData);
			ResponseEntity objResponseEntity = SerializationUtils.deserializeResponse(receiveData);
			serviceAccessEngine.setRequestResult(objResponseEntity);
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
				this.serviceAccessEngine.removeCachedChannel(objServiceOnExeptionEvent.getWorkingChannel());
				this.serviceAccessEngine.clearAllResult(objServiceOnExeptionEvent.getExceptionHappen());
			}
		}
		if(this.getNext() != null)
		{
			try
			{
				this.getNext().handleRequest(event);
			} catch (Exception e) {
				logger.error("there is an exception comes out: " + StringUtils.ExceptionStackTraceToString(e));
			}
		}
	}
}
