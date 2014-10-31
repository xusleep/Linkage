package service.framework.io.handlers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import service.framework.common.entity.ResponseEntity;
import service.framework.io.client.comsume.ConsumerBean;
import service.framework.io.client.comsume.RequestResultEntity;
import service.framework.io.event.ServiceEvent;
import service.framework.io.event.ServiceOnErrorEvent;
import service.framework.io.event.ServiceOnMessageReceiveEvent;
import service.framework.io.event.ServiceStartedEvent;
import service.framework.io.event.ServiceStartingEvent;
import service.framework.serialization.SerializeUtils;

/**
 * ������Ĭ�ϵ��¼�����handler
 * 
 * @author zhonxu
 *
 */
public class ClientReadWriteHandler implements Handler {
	private AtomicInteger aint = new AtomicInteger(0);
	private final ConsumerBean consumerBean;
	
	public ClientReadWriteHandler(ConsumerBean consumerBean){
		this.consumerBean = consumerBean;
	}
	
	@Override
	public void handleRequest(ServiceEvent event) throws IOException {
		// TODO Auto-generated method stub
		if (event instanceof ServiceOnMessageReceiveEvent) {
			try {
				ServiceOnMessageReceiveEvent objServiceOnMessageReceiveEvent = (ServiceOnMessageReceiveEvent) event;
				String receiveData = objServiceOnMessageReceiveEvent.getMessage();
				//System.out.println(" receive message ... " + receiveData);
				aint.incrementAndGet();
				//System.out.println("���������Ϊ:" + aint.get());
				ResponseEntity objResponseEntity = SerializeUtils.deserializeResponse(receiveData);
				System.out.println("objResponseEntity.getRequestID():" + objResponseEntity.getRequestID());
				RequestResultEntity result = this.consumerBean.getResultList().remove(objResponseEntity.getRequestID());
				result.setResponseEntity(objResponseEntity);
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
		
	}
}
