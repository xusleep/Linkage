package service.framework.handlers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import service.framework.common.SerializeUtils;
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
 * ������Ĭ�ϵ��¼�����handler
 * 
 * @author zhonxu
 *
 */
public class ServiceReadWriteHandler implements Handler {
	private AtomicInteger aint = new AtomicInteger(0);
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
				//System.out.println(" receive message ... " + receiveData);
				aint.incrementAndGet();
				RequestEntity objRequestEntity = SerializeUtils.deserializeRequest(receiveData);
				ResponseEntity objResponseEntity = this.providerBean.prcessRequest(objRequestEntity);
				ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(channel, objRequestEntity.getRequestID());
				//System.out.println(" send message ... " + SerializeUtils.serializeResponse(objResponseEntity));
				objServiceOnMessageWriteEvent.setMessage(SerializeUtils.serializeResponse(objResponseEntity));
				channel.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
				channel.getWorker().writeFromUser(channel);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(event instanceof ServiceOnChannelCloseExeptionEvent){
			System.out.println("ServiceReadWriteHandler ServiceOnExeptionEvent happned ...");
			ServiceOnChannelCloseExeptionEvent objServiceOnExeptionEvent = (ServiceOnChannelCloseExeptionEvent)event;
			objServiceOnExeptionEvent.getExceptionHappen().printStackTrace();
		}
		else if(event instanceof ServiceOnErrorEvent){
			System.out.println("�����˴���" + ((ServiceOnErrorEvent)event).getMsg());
		}
		else if(event instanceof ServiceStartingEvent){
            System.out.println("Server Service starting ...");
		}
		//����������������ע�ᵽ��������ȥ
		else if(event instanceof ServiceStartedEvent){
			//���ｫ���ע�ᵽ�������ĵĴ���
            System.out.println("Server Service started.");
		}
		//System.out.println("���������Ϊ:" + aint.get());
	}
}
