package service.framework.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import service.framework.common.SerializeUtils;
import service.framework.common.entity.ResponseEntity;
import service.framework.event.ServiceEvent;
import service.framework.event.ServiceOnChannelCloseExeptionEvent;
import service.framework.event.ServiceOnChannelIOExeptionEvent;
import service.framework.event.ServiceOnErrorEvent;
import service.framework.event.ServiceOnMessageReceiveEvent;
import service.framework.event.ServiceStartedEvent;
import service.framework.event.ServiceStartingEvent;
import service.framework.serviceaccess.ServiceAccess;

/**
 * the default handler for the client message received event
 * 
 * @author zhonxu
 *
 */
public class ClientReadWriteHandler implements Handler {
	private static Logger  logger = Logger.getLogger(ClientReadWriteHandler.class);  
	private final ServiceAccess consumerBean;
	
	public ClientReadWriteHandler(ServiceAccess consumerBean){
		this.consumerBean = consumerBean;
	}
	
	@Override
	public void handleRequest(ServiceEvent event) throws IOException {
		// TODO Auto-generated method stub
		if (event instanceof ServiceOnMessageReceiveEvent) {
			try {
				ServiceOnMessageReceiveEvent objServiceOnMessageReceiveEvent = (ServiceOnMessageReceiveEvent) event;
				String receiveData = objServiceOnMessageReceiveEvent.getMessage();
				ResponseEntity objResponseEntity = SerializeUtils.deserializeResponse(receiveData);
				objServiceOnMessageReceiveEvent.getWorkingChannel().setRequestResult(objResponseEntity);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("there is a error comes out: " + ((ServiceOnErrorEvent)event).getMsg());
			}
		}
		else if(event instanceof ServiceOnChannelCloseExeptionEvent ){
			ServiceOnChannelCloseExeptionEvent objServiceOnExeptionEvent = (ServiceOnChannelCloseExeptionEvent)event;
			this.consumerBean.removeCachedChannel(objServiceOnExeptionEvent.getWorkingChannel());
			objServiceOnExeptionEvent.getWorkingChannel().clearAllResult(objServiceOnExeptionEvent.getExceptionHappen());
		}
		else if(event instanceof ServiceOnChannelIOExeptionEvent){
			ServiceOnChannelIOExeptionEvent objServiceOnExeptionEvent = (ServiceOnChannelIOExeptionEvent)event;
			this.consumerBean.removeCachedChannel(objServiceOnExeptionEvent.getWorkingChannel());
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
