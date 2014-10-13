package service.framework.io.handlers;

import static service.framework.io.fire.Fires.fireCommonEvent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;

import service.framework.io.context.DefaultServiceContext;
import service.framework.io.context.ServiceContext;
import service.framework.io.event.ServiceEvent;
import service.framework.io.event.ServiceOnErrorEvent;
import service.framework.io.event.ServiceOnMessageReceiveEvent;
import service.framework.io.event.ServiceOnMessageWriteEvent;
import service.framework.io.event.ServiceStartedEvent;
import service.framework.io.event.ServiceStartingEvent;
import service.framework.io.fire.MasterHandler;
import service.framework.io.server.WorkingChannel;
import service.framework.protocol.ShareingProtocolData;
import service.framework.provide.ProviderBean;
import service.framework.provide.entity.RequestEntity;
import service.framework.provide.entity.ResponseEntity;
import service.framework.serialization.SerializeUtils;

/**
 * ������Ĭ�ϵ��¼�����handler
 * 
 * @author zhonxu
 *
 */
public class ReadWriteHandler implements Handler {
	private AtomicInteger aint = new AtomicInteger(0);
	
	private final ApplicationContext applicationContext;
	
	public ReadWriteHandler(ApplicationContext applicationContext){
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void handleRequest(ServiceContext context, ServiceEvent event) throws IOException {
		// TODO Auto-generated method stub
		if (event instanceof ServiceOnMessageReceiveEvent) {
			try {
				ServiceOnMessageReceiveEvent objServiceOnMessageReceiveEvent = (ServiceOnMessageReceiveEvent) event;
				WorkingChannel channel = objServiceOnMessageReceiveEvent.getSocketChannel();
				String receiveData = objServiceOnMessageReceiveEvent.getMessage();
				System.out.println(" receive message ... " + receiveData);
				aint.incrementAndGet();
				RequestEntity objRequestEntity = SerializeUtils.deserializeRequest(receiveData);
				ProviderBean objProviderBean = (ProviderBean)applicationContext.getBean(objRequestEntity.getServiceName());
				ResponseEntity objResponseEntity = objProviderBean.prcessRequest(objRequestEntity);
				//ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(channel);
				System.out.println(" send message ... " + SerializeUtils.serializeResponse(objResponseEntity));
				//objServiceOnMessageWriteEvent.setMessage(SerializeUtils.serializeResponse(objResponseEntity));
				//channel.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
				//channel.getWorker().writeFromUser(channel);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		System.out.println("���������Ϊ:" + aint.get());
		System.out.println("���г���Ϊ:" + MasterHandler.pool.size());
	}
}
