package service.middleware.linkage.framework.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.event.ServerOnFileDataReceivedEvent;
import service.middleware.linkage.framework.event.ServiceEvent;
import service.middleware.linkage.framework.event.ServiceExeptionEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageDataReceivedEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageReceiveEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageWriteEvent;
import service.middleware.linkage.framework.io.common.WorkingChannelContext;
import service.middleware.linkage.framework.io.nio.strategy.WorkingChannelMode;
import service.middleware.linkage.framework.io.nio.strategy.WorkingChannelModeSwitchState;
import service.middleware.linkage.framework.io.nio.strategy.WorkingChannelModeUtils;
import service.middleware.linkage.framework.io.nio.strategy.message.NIOMessageWorkingChannelStrategy;
import service.middleware.linkage.framework.io.protocol.IOProtocol;

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
					WorkingChannelContext workingChannel = objServiceOnMessageReceiveEvent.getWorkingChannel();
					synchronized(workingChannel)
					{
						// Check if switch the mode again
						if(WorkingChannelModeUtils.checkModeSwitch(receiveData))
						{
							WorkingChannelModeSwitchState workingChannelModeSwitchState = WorkingChannelModeUtils.getModeSwitchState(receiveData);
							WorkingChannelMode targetWorkingChannelMode = WorkingChannelModeUtils.getModeSwitch(receiveData);
							if(targetWorkingChannelMode != workingChannel.getWorkingChannelMode() && workingChannel.getWorkingChannelMode() == WorkingChannelMode.MESSAGEMODE)
							{
								NIOMessageWorkingChannelStrategy msgStrategy = (NIOMessageWorkingChannelStrategy)workingChannel.getWorkingChannelStrategy();
								// if just request, we will send back the request by ok
								if(workingChannelModeSwitchState == WorkingChannelModeSwitchState.REQUEST)
								{
									ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(workingChannel, null);
									// tell the client the switch is ok
									objServiceOnMessageWriteEvent.setMessage(WorkingChannelModeUtils.getModeSwitchString(targetWorkingChannelMode, WorkingChannelModeSwitchState.REQUESTOK));
									msgStrategy.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
									msgStrategy.writeChannel();
								}
								objServiceOnMessageReceiveEvent.getWorkingChannel().setWorkingStrategy(targetWorkingChannelMode);
							}
//							else if(targetWorkingChannelMode != workingChannel.getWorkingChannelMode() && workingChannel.getWorkingChannelMode() == WorkingChannelMode.FILEMODE)
//							{
//								// if just request, we will send back the request by ok
//								if(workingChannelModeSwitchState == WorkingChannelModeSwitchState.REQUEST)
//								{
//									NIOFileWorkingChannelStrategy msgStrategy = (NIOFileWorkingChannelStrategy)workingChannel.getWorkingChannelStrategy();
//									msgStrategy.writeMessage(WorkingChannelModeUtils.getModeSwitchString(targetWorkingChannelMode, WorkingChannelModeSwitchState.REQUESTOK));
//								}
//								objServiceOnMessageReceiveEvent.getWorkingChannel().switchWorkMode(targetWorkingChannelMode);
//							}
						}
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
		else if(event instanceof ServiceOnMessageDataReceivedEvent){
			ServiceOnMessageDataReceivedEvent objServiceOnMessageDataReceivedEvent = (ServiceOnMessageDataReceivedEvent)event;
			String receiveString = new String(objServiceOnMessageDataReceivedEvent.getMessageData(), IOProtocol.FRAMEWORK_IO_ENCODING);
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
