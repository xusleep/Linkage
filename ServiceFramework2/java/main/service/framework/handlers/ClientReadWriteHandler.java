package service.framework.handlers;

import java.io.IOException;

import service.framework.common.SerializeUtils;
import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ResponseEntity;
import service.framework.comsume.ConsumerBean;
import service.framework.event.ServiceEvent;
import service.framework.event.ServiceOnErrorEvent;
import service.framework.event.ServiceOnExeptionEvent;
import service.framework.event.ServiceOnMessageReceiveEvent;
import service.framework.event.ServiceStartedEvent;
import service.framework.event.ServiceStartingEvent;

/**
 * the default handler for the client message received event
 * 
 * @author zhonxu
 *
 */
public class ClientReadWriteHandler implements Handler {
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
				ResponseEntity objResponseEntity = SerializeUtils.deserializeResponse(receiveData);
				this.consumerBean.setRequestResult(objResponseEntity);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(event instanceof ServiceOnExeptionEvent){
			ServiceOnExeptionEvent objServiceOnExeptionEvent = (ServiceOnExeptionEvent)event;
			this.consumerBean.removeCachedChannel(objServiceOnExeptionEvent.getSocketChannel());
			if(objServiceOnExeptionEvent.getRequestID() != null)
			{
				this.consumerBean.setExceptionRuquestResult(objServiceOnExeptionEvent.getRequestID(), objServiceOnExeptionEvent.getExceptionHappen());
			}
		}
		else if(event instanceof ServiceOnErrorEvent){
			System.out.println("there is a error comes out" + ((ServiceOnErrorEvent)event).getMsg());
		}
		else if(event instanceof ServiceStartingEvent){
            System.out.println("Server Service starting ...");
		}
		//服务启动，将服务注册到服务中心去
		else if(event instanceof ServiceStartedEvent){
			//这里将添加注册到服务中心的代码
            System.out.println("Server Service started.");
		}
		
	}
}
