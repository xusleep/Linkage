package service.framework.handlers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import service.framework.common.SerializeUtils;
import service.framework.common.StringUtils;
import service.framework.common.entity.RequestEntity;
import service.framework.common.entity.ResponseEntity;
import service.framework.event.ServiceEvent;
import service.framework.event.ServiceOnChannelCloseExeptionEvent;
import service.framework.event.ServiceOnErrorEvent;
import service.framework.event.ServiceOnMessageReceiveEvent;
import service.framework.event.ServiceOnMessageWriteEvent;
import service.framework.event.ServiceStartedEvent;
import service.framework.event.ServiceStartingEvent;
import service.framework.io.common.WorkingChannel;
import service.framework.provide.ProviderBean;

/**
 * 这里是默认的事件处理handler
 * 
 * @author zhonxu
 *
 */
public class ServiceReadWriteHandler implements Handler {
	private static Logger  logger = Logger.getLogger(ClientReadWriteHandler.class); 
	private final ProviderBean  providerBean;
	
	public ServiceReadWriteHandler(ProviderBean providerBean){
		this.providerBean = providerBean;
	}
	
	@Override
	public void handleRequest(ServiceEvent event) throws IOException {
		// TODO Auto-generated method stub
		if (event instanceof ServiceOnMessageReceiveEvent) {
			try {
				ServiceOnMessageReceiveEvent objServiceOnMessageReceiveEvent = (ServiceOnMessageReceiveEvent) event;
				WorkingChannel channel = objServiceOnMessageReceiveEvent.getWorkingChannel();
				String receiveData = objServiceOnMessageReceiveEvent.getMessage();
				RequestEntity objRequestEntity = SerializeUtils.deserializeRequest(receiveData);
				ResponseEntity objResponseEntity = this.providerBean.prcessRequest(objRequestEntity);
				ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(channel, objRequestEntity.getRequestID());
				objServiceOnMessageWriteEvent.setMessage(SerializeUtils.serializeResponse(objResponseEntity));
				channel.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
				channel.getWorker().writeFromUser(channel);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
