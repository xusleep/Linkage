package service.middleware.linkage.framework.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.entity.ResponseEntity;
import service.middleware.linkage.framework.event.ServiceEvent;
import service.middleware.linkage.framework.event.ServiceOnChannelCloseExeptionEvent;
import service.middleware.linkage.framework.event.ServiceOnChannelIOExeptionEvent;
import service.middleware.linkage.framework.event.ServiceOnErrorEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageReceiveEvent;
import service.middleware.linkage.framework.event.ServiceStartedEvent;
import service.middleware.linkage.framework.event.ServiceStartingEvent;
import service.middleware.linkage.framework.io.common.NIOMessageWorkingChannelStrategy;
import service.middleware.linkage.framework.io.common.WorkingChannelMode;
import service.middleware.linkage.framework.serialization.SerializationUtils;
import service.middleware.linkage.framework.serviceaccess.ServiceAccess;

/**
 * the default handler for the client message received event
 * 
 * @author zhonxu
 *
 */
public class MessageClientReadWriteHandler implements Handler {
	private static Logger  logger = Logger.getLogger(MessageClientReadWriteHandler.class);  
	
	private final ServiceAccess serviceAccess;
	
	public MessageClientReadWriteHandler(ServiceAccess serviceAccess){
		this.serviceAccess = serviceAccess;
	}
	
	@Override
	public void handleRequest(ServiceEvent event) throws IOException {
		if (event instanceof ServiceOnMessageReceiveEvent) {
			try {
				ServiceOnMessageReceiveEvent objServiceOnMessageReceiveEvent = (ServiceOnMessageReceiveEvent) event;
				String receiveData = objServiceOnMessageReceiveEvent.getMessage();
				ResponseEntity objResponseEntity = SerializationUtils.deserializeResponse(receiveData);
				NIOMessageWorkingChannelStrategy strategy = (NIOMessageWorkingChannelStrategy)objServiceOnMessageReceiveEvent.getWorkingChannel().findWorkingChannelStrategy(WorkingChannelMode.MessageMode);
				strategy.setRequestResult(objResponseEntity);
			} catch (Exception e) {
				logger.error("there is a error comes out: " + ((ServiceOnErrorEvent)event).getMsg());
			}
		}
		else if(event instanceof ServiceOnChannelCloseExeptionEvent ){
			ServiceOnChannelCloseExeptionEvent objServiceOnExeptionEvent = (ServiceOnChannelCloseExeptionEvent)event;
			NIOMessageWorkingChannelStrategy strategy = (NIOMessageWorkingChannelStrategy)objServiceOnExeptionEvent.getWorkingChannel().findWorkingChannelStrategy(WorkingChannelMode.MessageMode);
			this.serviceAccess.removeCachedChannel(objServiceOnExeptionEvent.getWorkingChannel());
			strategy.clearAllResult(objServiceOnExeptionEvent.getExceptionHappen());
		}
		else if(event instanceof ServiceOnChannelIOExeptionEvent){
			ServiceOnChannelIOExeptionEvent objServiceOnExeptionEvent = (ServiceOnChannelIOExeptionEvent)event;
			NIOMessageWorkingChannelStrategy strategy = (NIOMessageWorkingChannelStrategy)objServiceOnExeptionEvent.getWorkingChannel().findWorkingChannelStrategy(WorkingChannelMode.MessageMode);
			this.serviceAccess.removeCachedChannel(objServiceOnExeptionEvent.getWorkingChannel());
			strategy.clearAllResult(objServiceOnExeptionEvent.getExceptionHappen());
		
		}
		else if(event instanceof ServiceOnErrorEvent){
			logger.error("there is a error comes out" + ((ServiceOnErrorEvent)event).getMsg());
		}
		else if(event instanceof ServiceStartingEvent){
			logger.debug("Server Service starting ...");
		}
		else if(event instanceof ServiceStartedEvent){
			logger.debug("Server Service starting ...");
		}
		
	}
}
