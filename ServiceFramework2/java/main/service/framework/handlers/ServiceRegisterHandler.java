package service.framework.handlers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import service.framework.common.SerializeUtils;
import service.framework.common.ShareingData;
import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ServiceInformationEntity;
import service.framework.comsume.ConsumerBean;
import service.framework.event.ServiceEvent;
import service.framework.event.ServiceStartedEvent;
import service.framework.properties.ServicePropertyEntity;

/***
 * ���handlerֻ������÷������ĵ�ʱ��ʹ��
 * ����������ʱ�򣬻ᷢ��һ��ServiceStartedEvent��������ܵ����Ժ�
 * ���д���������������ģ����͵�ǰ����ķ����б���Ϣ
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
	public void handleRequest(ServiceEvent event)  {
		// TODO Auto-generated method stub
		if (event instanceof ServiceStartedEvent) {
			
			// TODO Auto-generated method stub
			//ִ��
			ServiceStartedEvent objServiceOnReadEvent = (ServiceStartedEvent) event;
			List<ServiceInformationEntity> serviceInformationList = new LinkedList<ServiceInformationEntity>();
			//��ȡ���з��񣬽�����ע�ᵽע������
			List<ServicePropertyEntity> serviceList = this.consumerBean.getServicePropertyEntity().getServiceList();
			for(ServicePropertyEntity serviceEntity: serviceList)
			{
				String interfaceName = serviceEntity.getServiceInterface();
				try {
					Class interfaceclass = Class.forName(interfaceName);
					Method[] methods = interfaceclass.getMethods();
					for(int i = 0; i < methods.length; i++){
						ServiceInformationEntity subServiceInformation = new ServiceInformationEntity();
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
			RequestResultEntity result = this.consumerBean.prcessRequest(ShareingData.SERVICE_CENTER_REGISTER_ID, args, true);
			if(result.isException()){
				result.getException().printStackTrace();
			}
			else
			{
				System.out.println("register to the service center ..." + result.getResponseEntity().getResult());
			}
		}
	}
}
