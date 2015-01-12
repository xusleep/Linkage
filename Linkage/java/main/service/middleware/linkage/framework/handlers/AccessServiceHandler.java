package service.middleware.linkage.framework.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.WorkingChannelContext;
import service.middleware.linkage.framework.io.nio.strategy.message.NIOMessageWorkingChannelStrategy;
import service.middleware.linkage.framework.io.nio.strategy.message.events.ServiceOnMessageReceiveEvent;
import service.middleware.linkage.framework.io.nio.strategy.message.events.ServiceOnMessageWriteEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.NIOMixedStrategy;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnFileDataReceivedEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnMessageDataReceivedEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnMessageDataWriteEvent;
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
		if(event instanceof ServiceOnMessageDataReceivedEvent){
			try {
				ServiceOnMessageDataReceivedEvent objServiceOnMessageDataReceivedEvent = (ServiceOnMessageDataReceivedEvent)event;
				WorkingChannelContext channel = objServiceOnMessageDataReceivedEvent.getWorkingChannel();
				NIOMixedStrategy strategy = (NIOMixedStrategy) channel.getWorkingChannelStrategy();
				String receiveData = new String(objServiceOnMessageDataReceivedEvent.getMessageData(), EncodingUtils.FRAMEWORK_IO_ENCODING);
				RequestEntity objRequestEntity = SerializationUtils.deserializeRequest(receiveData);
				logger.debug("ServiceOnMessageDataReceivedEvent receive message : " + receiveData);
				ResponseEntity objResponseEntity = this.provider.acceptServiceRequest(objRequestEntity);
				ServiceOnMessageDataWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageDataWriteEvent(channel, SerializationUtils.serializeResponse(objResponseEntity).getBytes(EncodingUtils.FRAMEWORK_IO_ENCODING));
				strategy.offerMessageWriteQueue(objServiceOnMessageWriteEvent);
				strategy.writeChannel();
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
