package service.framework.io.handlers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import service.framework.io.client.comsume.ConsumerBean;
import service.framework.io.event.ServiceEvent;
import service.framework.io.event.ServiceStartedEvent;
import service.framework.serialization.SerializeUtils;
import service.properties.ServiceEntity;
import servicecenter.service.ServiceInformation;

/***
 * 这个handler只针对配置服务中心的时候使用
 * 服务启动的时候，会发送一个ServiceStartedEvent，这里接受到了以后
 * 进行处理，并且向服务中心，发送当前服务的服务列表信息
 * @author zhonxu
 *
 */
public class ServiceRegisterHandler implements Handler {
	private AtomicInteger aint = new AtomicInteger(0);
	private final ConsumerBean consumerBean;
	
	public ServiceRegisterHandler(ConsumerBean consumerBean){
		this.consumerBean = consumerBean;
	}
	
	@Override
	public void handleRequest(ServiceEvent event) throws IOException {
		// TODO Auto-generated method stub
		if (event instanceof ServiceStartedEvent) {
			
			// TODO Auto-generated method stub
			//执行
			ServiceStartedEvent objServiceOnReadEvent = (ServiceStartedEvent) event;
			List<ServiceInformation> serviceInformationList = new LinkedList<ServiceInformation>();
			//获取所有服务，将服务注册到注册中心
			List<ServiceEntity> serviceList = this.consumerBean.getServicePropertyEntity().getServiceList();
			for(ServiceEntity serviceEntity: serviceList)
			{
				String interfaceName = serviceEntity.getServiceInterface();
				try {
					Class interfaceclass = Class.forName(interfaceName);
					Method[] methods = interfaceclass.getMethods();
					for(int i = 0; i < methods.length; i++){
						ServiceInformation subServiceInformation = new ServiceInformation();
						subServiceInformation.setAddress(this.consumerBean.getServicePropertyEntity().getServiceAddress());
						subServiceInformation.setPort(this.consumerBean.getServicePropertyEntity().getServicePort());
						subServiceInformation.setServiceMethod(methods[i].getName());
						subServiceInformation.setServiceName(serviceEntity.getServiceName());
						subServiceInformation.setServiceVersion(serviceEntity.getServiceVersion());
						serviceInformationList.add(subServiceInformation);
						System.out.println("service name : " + serviceEntity.getServiceName() + " method name : " + methods[i].getName());
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			String strServiceInformation = SerializeUtils.serializeServiceInformationList(serviceInformationList);
			List<String> args = new LinkedList<String>();
			args.add(strServiceInformation);
			try {
				this.consumerBean.prcessRequest("serviceCenter", args);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
