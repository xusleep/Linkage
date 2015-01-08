package service.middleware.linkage.framework.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.event.ServiceEvent;
import service.middleware.linkage.framework.event.ServiceExeptionEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageReceiveEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageWriteEvent;
import service.middleware.linkage.framework.io.common.NIOMessageWorkingChannelStrategy;
import service.middleware.linkage.framework.io.common.WorkingChannelContext;
import service.middleware.linkage.framework.io.common.WorkingChannelMode;
import service.middleware.linkage.framework.io.common.WorkingChannelModeUtils;

/**
 * the default handler for the client message received event
 * 
 * @author zhonxu
 *
 */
public class NIOSinkHandler extends Handler {
	private static Logger  logger = Logger.getLogger(NIOSinkHandler.class);  
	
	public NIOSinkHandler(){
	}
	
	@Override
	public void handleRequest(ServiceEvent event) throws IOException {
		if (event instanceof ServiceOnMessageReceiveEvent) {
			try {
				ServiceOnMessageReceiveEvent objServiceOnMessageReceiveEvent = (ServiceOnMessageReceiveEvent) event;
				String receiveData = objServiceOnMessageReceiveEvent.getMessage();
				// check if the message contains the mode swtich information
				// if yes, send the switch mode change information back to client as well, 
				// so the client's working mode will change as well
				if(WorkingChannelModeUtils.checkModeSwitch(receiveData))
				{
					WorkingChannelMode targetWorkingChannelMode = WorkingChannelModeUtils.getModeSwitch(receiveData);
					WorkingChannelContext workingChannel = objServiceOnMessageReceiveEvent.getWorkingChannel();
					if(targetWorkingChannelMode != workingChannel.getWorkingChannelMode())
					{
						NIOMessageWorkingChannelStrategy msgStrategy = (NIOMessageWorkingChannelStrategy)workingChannel.getWorkingChannelStrategy();
						ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(workingChannel, null);
						objServiceOnMessageWriteEvent.setMessage(WorkingChannelModeUtils.getModeSwitchString(targetWorkingChannelMode));
						msgStrategy.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
						msgStrategy.writeChannel();
						objServiceOnMessageReceiveEvent.getWorkingChannel().switchWorkMode(targetWorkingChannelMode);
					}
				}
				else if(this.getNext() != null)
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
