package service.framework.io.handlers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;

import service.framework.io.context.ServiceContext;
import service.framework.io.event.ServiceEvent;
import service.framework.io.event.ServiceOnErrorEvent;
import service.framework.io.event.ServiceOnMessageReceiveEvent;
import service.framework.io.event.ServiceOnMessageWriteEvent;
import service.framework.io.event.ServiceStartedEvent;
import service.framework.io.event.ServiceStartingEvent;
import service.framework.io.server.WorkingChannel;
import service.framework.provide.ProviderBean;
import service.framework.provide.entity.RequestEntity;
import service.framework.provide.entity.ResponseEntity;
import service.framework.serialization.SerializeUtils;

/**
 * 这里是默认的事件处理handler
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
				if(channel.isOpen())
				{
					String receiveData = objServiceOnMessageReceiveEvent.getMessage();
					System.out.println(" receive message ... " + receiveData);
					aint.incrementAndGet();
					RequestEntity objRequestEntity = SerializeUtils.deserializeRequest(receiveData);
					ProviderBean objProviderBean = (ProviderBean)applicationContext.getBean(objRequestEntity.getServiceName());
					ResponseEntity objResponseEntity = objProviderBean.prcessRequest(objRequestEntity);
					ServiceOnMessageWriteEvent objServiceOnMessageWriteEvent = new ServiceOnMessageWriteEvent(channel);
					System.out.println(" send message ... " + SerializeUtils.serializeResponse(objResponseEntity));
					objServiceOnMessageWriteEvent.setMessage(SerializeUtils.serializeResponse(objResponseEntity));
					channel.writeBufferQueue.offer(objServiceOnMessageWriteEvent);
					channel.getWorker().writeFromUser(channel);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(event instanceof ServiceOnErrorEvent){
			System.out.println("出现了错误" + ((ServiceOnErrorEvent)event).getMsg());
		}
		else if(event instanceof ServiceStartingEvent){
            System.out.println("Server Service starting ...");
		}
		//服务启动，将服务注册到服务中心去
		else if(event instanceof ServiceStartedEvent){
			//这里将添加注册到服务中心的代码
            System.out.println("Server Service started.");
		}
		System.out.println("处理的条数为:" + aint.get());
	}
}
