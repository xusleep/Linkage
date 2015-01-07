package service.middleware.linkage.framework.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.common.entity.RequestEntity;
import service.middleware.linkage.framework.common.entity.ResponseEntity;
import service.middleware.linkage.framework.event.ServiceEvent;
import service.middleware.linkage.framework.event.ServiceOnChannelCloseExeptionEvent;
import service.middleware.linkage.framework.event.ServiceOnErrorEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageReceiveEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageWriteEvent;
import service.middleware.linkage.framework.io.common.NIOMessageWorkingChannelStrategy;
import service.middleware.linkage.framework.io.common.WorkingChannelContext;
import service.middleware.linkage.framework.io.common.WorkingChannelMode;
import service.middleware.linkage.framework.provider.ServiceProvider;
import service.middleware.linkage.framework.serialization.SerializationUtils;

/**
 * Service handler from the server side
 * 
 * @author zhonxu
 *
 */
public class NIOMessageServiceReadWriteHandler implements Handler {
	private static Logger  logger = Logger.getLogger(NIOMessageClientReadWriteHandler.class); 
	private final ServiceProvider  provider;
	
	public NIOMessageServiceReadWriteHandler(ServiceProvider provider){
		this.provider = provider;
	}
	
	@Override
	public void handleRequest(ServiceEvent event) throws IOException {
		if (event instanceof ServiceOnMessageReceiveEvent) {
			try {
				ServiceOnMessageReceiveEvent objServiceOnMessageReceiveEvent = (ServiceOnMessageReceiveEvent) event;
				WorkingChannelContext channel = objServiceOnMessageReceiveEvent.getWorkingChannel();
				NIOMessageWorkingChannelStrategy strategy = (NIOMessageWorkingChannelStrategy) channel.findWorkingChannelStrategy(WorkingChannelMode.MessageMode);
				String receiveData = objServiceOnMessageReceiveEvent.getMessage();
				RequestEntity objRequestEntity = SerializationUtils.deserializeRequest(receiveData);
				ResponseEntity objResponseEntity = this.provider.acceptServiceRequest(objRequestEntity);
				ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(channel, objRequestEntity.getRequestID());
				objServiceOnMessageWriteEvent.setMessage(SerializationUtils.serializeResponse(objResponseEntity));
				strategy.offerWriterQueue(objServiceOnMessageWriteEvent);
				strategy.write();
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
	}
}
