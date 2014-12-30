package service.middleware.linkage.framework.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.SerializeUtils;
import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.common.entity.RequestEntity;
import service.middleware.linkage.framework.common.entity.ResponseEntity;
import service.middleware.linkage.framework.event.ServiceEvent;
import service.middleware.linkage.framework.event.ServiceOnChannelCloseExeptionEvent;
import service.middleware.linkage.framework.event.ServiceOnErrorEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageReceiveEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageWriteEvent;
import service.middleware.linkage.framework.event.ServiceStartedEvent;
import service.middleware.linkage.framework.event.ServiceStartingEvent;
import service.middleware.linkage.framework.io.common.WorkingChannel;
import service.middleware.linkage.framework.provider.Provider;

/**
 * Service handler from the server side
 * 
 * @author zhonxu
 *
 */
public class ServiceReadWriteHandler implements Handler {
	private static Logger  logger = Logger.getLogger(ClientReadWriteHandler.class); 
	private final Provider  providerBean;
	
	public ServiceReadWriteHandler(Provider providerBean){
		this.providerBean = providerBean;
	}
	
	@Override
	public void handleRequest(ServiceEvent event) throws IOException {
		if (event instanceof ServiceOnMessageReceiveEvent) {
			try {
				ServiceOnMessageReceiveEvent objServiceOnMessageReceiveEvent = (ServiceOnMessageReceiveEvent) event;
				WorkingChannel channel = objServiceOnMessageReceiveEvent.getWorkingChannel();
				String receiveData = objServiceOnMessageReceiveEvent.getMessage();
				RequestEntity objRequestEntity = SerializeUtils.deserializeRequest(receiveData);
				ResponseEntity objResponseEntity = this.providerBean.acceptServiceRequest(objRequestEntity);
				ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(channel, objRequestEntity.getRequestID());
				objServiceOnMessageWriteEvent.setMessage(SerializeUtils.serializeResponse(objResponseEntity));
				channel.offerWriterQueue(objServiceOnMessageWriteEvent);
				channel.getWorker().writeFromUser(channel);
			} catch (Exception e) {
				logger.error("ServiceReadWriteHandler exception happned ..." + StringUtils.ExceptionStackTraceToString(((ServiceOnChannelCloseExeptionEvent) event).getExceptionHappen()));
			}
		}
		else if(event instanceof ServiceOnChannelCloseExeptionEvent){
			logger.error("ServiceReadWriteHandler ServiceOnExeptionEvent happned ..." + StringUtils.ExceptionStackTraceToString(((ServiceOnChannelCloseExeptionEvent) event).getExceptionHappen()));
			ServiceOnChannelCloseExeptionEvent objServiceOnExeptionEvent = (ServiceOnChannelCloseExeptionEvent)event;
			objServiceOnExeptionEvent.getExceptionHappen().printStackTrace();
		}
		else if(event instanceof ServiceOnErrorEvent){
			logger.error("ServiceReadWriteHandler ServiceOnExeptionEvent happned ..." + StringUtils.ExceptionStackTraceToString(((ServiceOnChannelCloseExeptionEvent) event).getExceptionHappen()));
		}
		else if(event instanceof ServiceStartingEvent){
			logger.debug("Server Service starting ...");
		}
		else if(event instanceof ServiceStartedEvent){
			logger.debug("Server Service starting ...");
		}
	}
}
