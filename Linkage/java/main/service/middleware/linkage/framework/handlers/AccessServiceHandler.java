package service.middleware.linkage.framework.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.WorkingChannelContext;
import service.middleware.linkage.framework.io.nio.strategy.message.NIOMessageWorkingChannelStrategy;
import service.middleware.linkage.framework.io.nio.strategy.message.events.ServiceOnMessageReceiveEvent;
import service.middleware.linkage.framework.io.nio.strategy.message.events.ServiceOnMessageWriteEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServerOnFileDataReceivedEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceExeptionEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnMessageDataReceivedEvent;
import service.middleware.linkage.framework.provider.ServiceProvider;
import service.middleware.linkage.framework.serialization.SerializationUtils;
import service.middleware.linkage.framework.serviceaccess.entity.RequestEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ResponseEntity;
import service.middleware.linkage.framework.utils.EncodingUtils;
import service.middleware.linkage.framework.utils.StringUtils;

/**
 * Service handler from the server side
 * 
 * @author zhonxu
 *
 */
public class AccessServiceHandler extends Handler {
	private static Logger  logger = Logger.getLogger(AccessClientHandler.class); 
	private final ServiceProvider  provider;
	
	public AccessServiceHandler(ServiceProvider provider){
		this.provider = provider;
	}
	
	@Override
	public void handleRequest(ServiceEvent event) throws IOException {
		if (event instanceof ServiceOnMessageReceiveEvent) {
			try {
				ServiceOnMessageReceiveEvent objServiceOnMessageReceiveEvent = (ServiceOnMessageReceiveEvent) event;
				WorkingChannelContext channel = objServiceOnMessageReceiveEvent.getWorkingChannel();
				NIOMessageWorkingChannelStrategy strategy = (NIOMessageWorkingChannelStrategy) channel.getWorkingChannelStrategy();
				String receiveData = objServiceOnMessageReceiveEvent.getMessage();
				RequestEntity objRequestEntity = SerializationUtils.deserializeRequest(receiveData);
				ResponseEntity objResponseEntity = this.provider.acceptServiceRequest(objRequestEntity);
				ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(channel, objRequestEntity.getRequestID());
				objServiceOnMessageWriteEvent.setMessage(SerializationUtils.serializeResponse(objResponseEntity));
				strategy.offerWriterQueue(objServiceOnMessageWriteEvent);
				strategy.writeChannel();
				if(this.getNext() != null)
				{
					this.getNext().handleRequest(event);
				}
			} catch (Exception e) {
				logger.error("there is an exception comes out: " + StringUtils.ExceptionStackTraceToString(e));
			}
		}
		else if(event instanceof ServiceOnMessageDataReceivedEvent){
			ServiceOnMessageDataReceivedEvent objServiceOnMessageDataReceivedEvent = (ServiceOnMessageDataReceivedEvent)event;
			String receiveString = new String(objServiceOnMessageDataReceivedEvent.getMessageData(), EncodingUtils.FRAMEWORK_IO_ENCODING);
			logger.debug("ServiceOnMessageDataReceivedEvent receive message : " + receiveString);
		}
		else if(event instanceof ServerOnFileDataReceivedEvent){
			ServerOnFileDataReceivedEvent objServerOnFileDataReceivedEvent = (ServerOnFileDataReceivedEvent)event;
			logger.debug("ServerOnFileDataReceivedEvent receive message : " + objServerOnFileDataReceivedEvent.getFileID());
		}
		else if(event instanceof ServiceExeptionEvent ){
			ServiceExeptionEvent objServiceOnExeptionEvent = (ServiceExeptionEvent)event;
			logger.error("there is an exception comes out: " + StringUtils.ExceptionStackTraceToString(objServiceOnExeptionEvent.getExceptionHappen()));
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
