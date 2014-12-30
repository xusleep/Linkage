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
import service.middleware.linkage.framework.serialization.SerializationUtils;
import service.middleware.linkage.framework.serviceaccess.ServiceAccess;

/**
 * the default handler for the client message received event
 * 
 * @author zhonxu
 *
 */
public class ClientReadWriteHandler implements Handler {
	private static Logger  logger = Logger.getLogger(ClientReadWriteHandler.class);  
	private final ServiceAccess serviceAccess;
	
	public ClientReadWriteHandler(ServiceAccess serviceAccess){
		this.serviceAccess = serviceAccess;
	}
	
	@Override
	public void handleRequest(ServiceEvent event) throws IOException {
		// TODO Auto-generated method stub
		if (event instanceof ServiceOnMessageReceiveEvent) {
			try {
				ServiceOnMessageReceiveEvent objServiceOnMessageReceiveEvent = (ServiceOnMessageReceiveEvent) event;
				String receiveData = objServiceOnMessageReceiveEvent.getMessage();
				ResponseEntity objResponseEntity = SerializationUtils.deserializeResponse(receiveData);
				objServiceOnMessageReceiveEvent.getWorkingChannel().setRequestResult(objResponseEntity);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("there is a error comes out: " + ((ServiceOnErrorEvent)event).getMsg());
			}
		}
		else if(event instanceof ServiceOnChannelCloseExeptionEvent ){
			ServiceOnChannelCloseExeptionEvent objServiceOnExeptionEvent = (ServiceOnChannelCloseExeptionEvent)event;
			this.serviceAccess.removeCachedChannel(objServiceOnExeptionEvent.getWorkingChannel());
			objServiceOnExeptionEvent.getWorkingChannel().clearAllResult(objServiceOnExeptionEvent.getExceptionHappen());
		}
		else if(event instanceof ServiceOnChannelIOExeptionEvent){
			ServiceOnChannelIOExeptionEvent objServiceOnExeptionEvent = (ServiceOnChannelIOExeptionEvent)event;
			this.serviceAccess.removeCachedChannel(objServiceOnExeptionEvent.getWorkingChannel());
			objServiceOnExeptionEvent.getWorkingChannel().clearAllResult(objServiceOnExeptionEvent.getExceptionHappen());
		
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
